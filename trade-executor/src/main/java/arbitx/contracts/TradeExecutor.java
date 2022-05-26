package arbitx.contracts;

import score.Context;
import score.VarDB;
import score.Address;
import score.annotation.External;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.math.BigInteger;

import com.iconloop.score.util.EnumerableSet;


public class TradeExecutor {
    private final String name;

    // External contracts.
    private final VarDB<Address> balancedDex = Context.newVarDB("balancedDex", Address.class);
    private final VarDB<Address> convexusSwapRouter = Context.newVarDB("convexusSwapRouter", Address.class);
    private final VarDB<Address> arbitrageSettings = Context.newVarDB("arbitrageSettings", Address.class);

    // Track token balances of this contract.
    private final EnumerableSet<Address> tokenAddresses = new EnumerableSet<Address>("tokenAddresses", Address.class);

    // Current ongoing arbitrage.
    private final VarDB<Boolean> ongoingArbitrage = Context.newVarDB("ongoingArbitrage", Boolean.class);
    private final VarDB<BigInteger> receivedTokenAmount = Context.newVarDB("receivedTokenAmount", BigInteger.class);

    public TradeExecutor(String name) {
        this.name = name;
        this.balancedDex.set(Address.fromString("cx648a6d9c5f231f6b86c0caa9cc9eff8bd6040999"));
        this.convexusSwapRouter.set(Address.fromString("cx1e6129d4ff4fc5e58daae80966cbb4b1a4f8ea7c"));
    }

    @External(readonly = true)
    public String name() {
        return name;
    }

    @External
    public BigInteger executeArbitrage(
        String buyExchange, 
        String sellExchange, 
        Address buyToken, 
        Address sellToken,
        BigInteger convexusFee, 
        BigInteger amount
        ) {
        Context.require(Context.getCaller().equals(this.arbitrageSettings.get()));
        this.ongoingArbitrage.set(true);

        if (buyExchange.equals("convexus")) {
            // Sell tokens on Balanced; Buy tokens from Convexus.
            this.swapBalanced(sellToken, buyToken, amount);
            this.swapConvexus(
                buyToken,
                sellToken, 
                convexusFee,
                this.receivedTokenAmount.get(),
                Context.getAddress(), 
                BigInteger.valueOf(Context.getBlockTimestamp()), // Microseconds, should be in seconds.
                BigInteger.ZERO,
                BigInteger.ZERO
            );
        }

        else {
            // Sell tokens on Convexus; Buy tokens on Balanced. 
            this.swapConvexus(
                sellToken, 
                buyToken, 
                convexusFee, 
                amount, 
                Context.getAddress(), 
                BigInteger.valueOf(Context.getBlockTimestamp()), // Microseconds, should be in seconds.
                BigInteger.ZERO,
                BigInteger.ZERO
            );
            this.swapBalanced(buyToken, sellToken, this.receivedTokenAmount.get());  
        }

        // Compute arbitrage result. Revert if arbitrage was not profitable.
        BigInteger arbitrageResult = this.receivedTokenAmount.get().subtract(amount);
        if (arbitrageResult.compareTo(BigInteger.ZERO) <= 0) {
            Context.revert("Arbitrage not profitable.");
        }

        // Reset variables keeping track of arbitrage.
        this.ongoingArbitrage.set(false);
        this.receivedTokenAmount.set(BigInteger.ZERO);

        // Return profit.
        return arbitrageResult;
    }

    @External
    public void tokenFallback (Address from, BigInteger value, byte[] data) {
        // Add tokenaddress to set. Used for tracking contract balance.
        this.tokenAddresses.add(Context.getCaller());

        // Execute if there is ongoing arbitrage.
        if (this.ongoingArbitrage.getOrDefault(false)) {
            this.receivedTokenAmount.set(value);
        }
    }

    // Tested. Works.
    @External(readonly = true)
    public Token[] getContractTokenBalances() {
        Integer numberOfAddresses = this.tokenAddresses.length();
        Token[] tokens = new Token[numberOfAddresses];

        for (Integer i = 0; i < numberOfAddresses; i++) {
            Address tokenAddress = this.tokenAddresses.at(i);
            Token token = new Token(tokenAddress);
            tokens[i] = token;
        }
        
        return tokens;
    }

    private void transferToken(Address token, Address to, BigInteger amount, byte[] data) {
        Context.call(token, "transfer", to, amount, data);
    }

    // Tested, works.
    @External
    public void swapBalanced(
        Address tokenIn,
        Address tokenOut,
        BigInteger amountIn
    ) {
        byte[] data = createBalancedSwapData(tokenOut);
        this.transferToken(tokenIn, balancedDex.get(), amountIn, data);
    }

    // Tested, works.
    @External
    public void swapConvexus(
        Address tokenIn, 
        Address tokenOut, 
        BigInteger fee, 
        BigInteger amountIn,
        Address recipient,
        BigInteger deadline,
        BigInteger amountOutMinimum,
        BigInteger sqrtPriceLimitX96
    ) {
        byte[] data = createConvexusSwapData(
            tokenOut, 
            fee, 
            recipient, 
            deadline, 
            amountOutMinimum, 
            sqrtPriceLimitX96
        );
        this.transferToken(tokenIn, convexusSwapRouter.get(), amountIn, data);
    }

    // Works.
    private byte[] createConvexusSwapData(
        Address tokenOut,
        BigInteger fee,
        Address recipient,
        BigInteger deadline, // Must be set. Unix time of when tx is not valid anymore.
        BigInteger amountOutMinimum,
        BigInteger sqrtPriceLimitX96
    ) {
        JsonObject data = Json.object();
        JsonObject params = Json.object();
        data.add("method", "exactInputSingle");
        data.add("params", params);
        params.add("tokenOut", tokenOut.toString());
        params.add("fee", fee.toString(10));
        params.add("recipient", recipient.toString());
        params.add("deadline", deadline.toString(10));
        params.add("amountOutMinimum", amountOutMinimum.toString(10));
        params.add("sqrtPriceLimitX96", sqrtPriceLimitX96.toString(10));
        return data.toString().getBytes();
    }

    // Works.
    private byte[] createBalancedSwapData(Address toToken) {
        JsonObject data = Json.object();
        data.add("method", "_swap");
        data.add("params", Json.object().add("toToken", toToken.toString()));
        return data.toString().getBytes();
    }
}
