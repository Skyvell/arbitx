package arbitx.contracts;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;


public class Pair {
    public String name;
    public Address tokenA;
    public Address tokenB;
    public BigInteger convexusFee;
    public BigInteger arbitrageThreshold;
    public BigInteger tokensPerIteration;

    public Pair() {
    }

    public Pair(String name, Address tokenA, Address tokenB, BigInteger convexusFee, BigInteger arbitrageThreshold, BigInteger tokensPerIteration) {
        this.name = name;
        this.tokenA = tokenA;
        this.tokenB = tokenB;
        this.convexusFee = convexusFee;
        this.arbitrageThreshold = arbitrageThreshold;
        this.tokensPerIteration = tokensPerIteration;
    }

    public void evaluateArbitrage() {
        //Convexus.getPrice();
        //Balanced.getPrice();
        //return evaluation;
    }

    public static void writeObject(ObjectWriter w, Pair p) {
        w.beginList(6);
        w.write(p.name);
        w.write(p.tokenA);
        w.write(p.tokenB);
        w.write(p.convexusFee);
        w.write(p.arbitrageThreshold);
        w.write(p.tokensPerIteration);
        w.end();
    }

    public static Pair readObject(ObjectReader r) {
        r.beginList();
        Pair p = new Pair(
            r.readString(),
            r.readAddress(),
            r.readAddress(),
            r.readBigInteger(),
            r.readBigInteger(),
            r.readBigInteger());
        r.end();
        return p;
    }
}

