package arbitx.contracts;

import score.ObjectReader;
import score.ObjectWriter;

import java.math.BigInteger;


public class ArbitrageIterations {
    public BigInteger minIterations;
    public BigInteger maxIterations;

    public ArbitrageIterations() {
    }

    public ArbitrageIterations(BigInteger minIterations, BigInteger maxIterations) {
        this.minIterations = minIterations;
        this.maxIterations = maxIterations;
    }
    
    public static void writeObject(ObjectWriter w, ArbitrageIterations a) {
        w.beginList(2);
        w.write(a.minIterations);
        w.write(a.maxIterations);
        w.end();
    }

    public static ArbitrageIterations readObject(ObjectReader r) {
        r.beginList();
        ArbitrageIterations a = new ArbitrageIterations(
            r.readBigInteger(),
            r.readBigInteger()
        );
        r.end();
        return a;
    }
}