package arbitx.contracts;

import score.Context;
import score.VarDB;
import score.Address;
import score.annotation.EventLog;
import score.annotation.External;

import scorex.util.ArrayList;

import java.math.BigInteger;

import java.util.Map;
import java.util.List;


public class CrossdexArbitrage {
    private final String name;
    
    private final VarDB<Address> balancedDex = Context.newVarDB("balancedDex", Address.class);
    private final VarDB<Address> convexusPoolFactory = Context.newVarDB("convexusPoolFactory", Address.class);
    private final VarDB<Address> arbitrageExecutor = Context.newVarDB("ArbitrageExecutor", Address.class);

    private final VarDB<ArbitrageIterations> arbitrageIterations = Context.newVarDB("arbitrageIterations", ArbitrageIterations.class);

    
    private final EnumerableMap<String, Pair> pairs = new EnumerableMap<String, Pair>("pairs", String.class, Pair.class);

    public CrossdexArbitrage(String name) {
        this.name = name;
        this.balancedDex.set(Address.fromString("cx648a6d9c5f231f6b86c0caa9cc9eff8bd6040999"));
        this.arbitrageExecutor.set(Address.fromString("cx7d625c19e786cab96a872f777b30696acbb71068"));
        this.convexusPoolFactory.set(Address.fromString("cx4d21f894d5c2f1f172e5b6aed171dd650d3165f6"));
        this.addPair(
            "sicx/bnusd", 
            Address.fromString("cx70806fdfa274fe12ab61f1f98c5a7a1409a0c108"), 
            Address.fromString("cx5838cb516d6156a060f90e9a3de92381331ff024"), 
            BigInteger.valueOf(3000), 
            BigInteger.valueOf(100),
            BigInteger.valueOf(2).multiply(Constants.EXA));
    }
    
    @External(readonly = true)
    public String name() {
        return name;
    }

    @External
    public void setArbitrageIterations(BigInteger minIterations, BigInteger maxIterations) {
        ArbitrageIterations iterations = new ArbitrageIterations(minIterations, maxIterations);
        this.arbitrageIterations.set(iterations);
    }

    @External
    public ArbitrageIterations getArbitrageIterations() {
        return this.arbitrageIterations.get();
    }

    // Token A quoted in token B.
    @External
    public void addPair(String name, Address tokenA, Address tokenB, BigInteger convexusFee, BigInteger arbitrageThreshold, BigInteger tokensPerIteration) {
        Pair pair = new Pair(name, tokenA, tokenB, convexusFee, arbitrageThreshold, tokensPerIteration);
        pairs.set(name, pair);
    }

    @External
    public void removePair(String name) {
        pairs.remove(name);
    }

    @External(readonly = true)
    public Pair getPair(String name) {
        return this.pairs.get(name);
    }

    @External(readonly = true)
    public List<Pair> getPairs() {
        int numberOfPairs = pairs.size();
        ArrayList<Pair> pairList = new ArrayList<>();

        for (int i = 0; i < numberOfPairs; i++) {
            String key = this.pairs.getKey(i);
            pairList.add(this.pairs.get(key));
        }

        return pairList;
    }

    @External(readonly = true)
    public List<ArbitrageEvaluation> getDetailedArbitrageEvaluation() {
        List<Pair> pairs = this.getPairs();
        ArbitrageEvaluation eval;

        List<ArbitrageEvaluation> arbitrageEvaluationList = new ArrayList<>();
        for (Pair pair : pairs) {
            eval = evaluateArbitrage(pair);
            arbitrageEvaluationList.add(eval);
        }
        return arbitrageEvaluationList;
    }

    @External(readonly = true)
    public List<String> checkForArbitrage() {
        List<Pair> pairs = this.getPairs();
        ArbitrageEvaluation eval;

        List<String> availableArbitrage = new ArrayList<>();
        for (Pair pair : pairs) {
            eval = evaluateArbitrage(pair);
            if (eval.arbitrageOpportunity) {
                availableArbitrage.add(pair.name);
            }
        }
        return availableArbitrage;
    }

    @External
    public void arbitrage(String pairName) {
        Pair pair = this.pairs.get(pairName);
        ArbitrageEvaluation eval = evaluateArbitrage(pair);
        Context.require(eval.arbitrageOpportunity, "No arbitrage opportunity.");

        BigInteger profit = BigInteger.ZERO;
        Integer iterations = 0;
        while (true) {
            try {
                profit = profit.add(
                    (BigInteger) Context.call(
                        this.arbitrageExecutor.get(),
                        "executeArbitrage",
                        eval.buyExchange, 
                        eval.sellExchange, 
                        pair.tokenA,
                        pair.tokenB,
                        pair.convexusFee, 
                        pair.tokensPerIteration)
                    );
            }
            catch (Exception e) {
                break;
            }
            iterations++;

            if (iterations > 4) {
                break;
            }
        }

        ArbitrageReport(pair.name, profit, BigInteger.valueOf(iterations));


        // Report result?
        
        // 1. Get prices from exchanges.
        // 2. Price difference larger than threshold?
        // 3. Call executeArbitrage in TradeExecuter contract until arbitrage no longer profitable.
        // 5. Update number of tokens to trade per iteration if outside of some interval.
        // 4. Report result via eventlog? Could check balance before and after, report difference.

    }

    @External
    public void tokenFallback (Address from, BigInteger value, byte[] data) {
        return;
    }


    // ===========================  Helper functions   ==========================================

   
    @SuppressWarnings("unchecked")
    private BigInteger getBalancedPrice(Pair pair) {
        BigInteger poolID = (BigInteger) Context.call(this.balancedDex.get(), "getPoolId", pair.tokenA, pair.tokenB);
        Map<String, Object> poolData = (Map<String, Object>) Context.call(this.balancedDex.get(), "getPoolStats", poolID);
        Address baseCurrency = (Address) poolData.get("base_token");
        BigInteger price = (BigInteger) poolData.get("price");

        if (pair.tokenA.equals(baseCurrency)) {
           return price;
        }
        else {
            return Utils.pow(BigInteger.TEN, BigInteger.valueOf(36)).divide(price);
        }
    }

    @SuppressWarnings("unchecked")
    private BigInteger getConvexusPrice(Pair pair) {
        Address poolAddress = (Address) Context.call(this.convexusPoolFactory.get(), "getPool", pair.tokenA, pair.tokenB, pair.convexusFee);
        Address token0 = (Address) Context.call(poolAddress, "token0");
        Map<String, Object> slot0 = (Map<String, Object>) Context.call(poolAddress, "slot0");
        BigInteger sqrtPrice = (BigInteger) slot0.get("sqrtPriceX96");
        BigInteger price = Utils.sqrtPriceX96ToPrice(sqrtPrice);

        if (pair.tokenA.equals(token0)) {
            return price;
        }
        else {
            return Utils.pow(BigInteger.TEN, BigInteger.valueOf(36)).divide(price);
        }
    }

    private ArbitrageEvaluation evaluateArbitrage(Pair pair) {
        BigInteger convexusPrice = getConvexusPrice(pair);
        BigInteger balancedPrice = getBalancedPrice(pair);
        ArbitrageEvaluation eval = new ArbitrageEvaluation(pair, convexusPrice, balancedPrice);
        return eval;
    }


    // ==============================   Eventlogs   =================================================


    @EventLog(indexed=3)
    protected void ArbitrageReport(String pairName, BigInteger profit, BigInteger iterations) {}
}
