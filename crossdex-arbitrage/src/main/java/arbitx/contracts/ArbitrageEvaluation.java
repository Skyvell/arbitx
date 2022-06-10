package arbitx.contracts;


import java.math.BigInteger;


public class ArbitrageEvaluation {
    public String name;
    public BigInteger convexusPrice;
    public BigInteger balancedPrice;
    public BigInteger percentDifference;
    public Boolean arbitrageOpportunity;
    public String buyExchange;
    public String sellExchange;

    public ArbitrageEvaluation() {
        
    }

    public ArbitrageEvaluation(Pair pair, BigInteger convexusPrice, BigInteger balancedPrice) {
        this.name = pair.name;
        this.convexusPrice = convexusPrice;
        this.balancedPrice = balancedPrice;
        this.percentDifference = computePriceDifferenceInPercentage();
        this.arbitrageOpportunity = checkArbitrageOpportunity(pair);
        this.assignSellAndBuyExchanges();
    }

    private BigInteger computePriceDifferenceInPercentage() {
        // Result in basis points. E.g. 200 -> Difference between Convexus and Balanced price is 2%.
        BigInteger nominator = this.balancedPrice.subtract(this.convexusPrice).abs().multiply(Constants.BASISPOINTS);
        BigInteger denominator = this.balancedPrice.add(this.convexusPrice).divide(BigInteger.TWO);
        BigInteger percentageDifference = nominator.divide(denominator);
        return percentageDifference;
    }

    private Boolean checkArbitrageOpportunity(Pair pair) {
        if (percentDifference.compareTo(pair.arbitrageThreshold) > 0) {
            return true;
        }
        else {
            return false;
        }
    }


    // I changed around balanced and convexus. Thinking is wrong with quote/base.
    private void assignSellAndBuyExchanges() {
        if (this.convexusPrice.compareTo(this.balancedPrice) < 0) {
            this.buyExchange = "balanced";
            this.sellExchange = "convexus";
        }
        else {
            this.buyExchange = "convexus";
            this.sellExchange = "balanced";
        }
    }
}
