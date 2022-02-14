package arbitx.contracts;

import score.Address;

import java.util.List;

import com.iconloop.score.util.EnumerableIntMap;
import com.iconloop.score.util.EnumerableSet;


public class Pair {
    String name;
    Address baseCurrency;
    Address quoteCurrency;
    List<Address> listedOn;

    public Pair(String name, Address baseCurrency, Address quoteCurrency, List<Address> listedOn) {
        this.name = name;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.listedOn = listedOn;
    }

    public void getLiquidityPoolDataBalanced() {
        // Get LP data from balanced.
    }

    public void getLiquidityPoolDataEquality() {
        // Get LP data from equality.
    }

}
