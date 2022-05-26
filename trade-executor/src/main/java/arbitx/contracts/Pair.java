package arbitx.contracts;

import score.Address;
import score.ObjectReader;
import score.ObjectWriter;

import java.util.List;

import com.iconloop.score.util.EnumerableIntMap;
import com.iconloop.score.util.EnumerableSet;


public class Pair {
    String name;
    Address tokenA;
    Address tokenB;

    public Pair(String name, Address tokenA, Address tokenB) {
        this.name = name;
        this.tokenA = tokenA;
        this.tokenB = tokenB;
    }

    public static void writeObject(ObjectWriter w, Pair p) {
        w.beginList(3);
        w.write(p.name);
        w.write(p.tokenA);
        w.write(p.tokenB);
        w.end();
    }

    public static Pair readObject(ObjectReader r) {
        r.beginList();
        Pair p = new Pair(
            r.readString(),
            r.readAddress(),
            r.readAddress());
        r.end();
        return p;
    }

    public void getLiquidityPoolDataBalanced() {
        // Get LP data from balanced.
    }

    public void getLiquidityPoolDataEquality() {
        // Get LP data from equality.
    }
}
