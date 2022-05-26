package arbitx.contracts;

import java.math.BigInteger;

import score.Address;
import score.VarDB;
import score.Context;

import java.util.Map;

public class Balanced {
    public final VarDB<Address> balancedDex = Context.newVarDB("balancedDex", Address.class);

    public BigInteger getPoolId(Address tokenA, Address tokenB) {
        return (BigInteger) Context.call(this.balancedDex.get(), "getPoolId", tokenA, tokenB);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPoolStats(BigInteger poolId) {
        return (Map<String, Object>) Context.call(this.balancedDex.get(), "getPoolStats", poolId);
    }

    public BigInteger getPrice(Address tokenA, Address tokenB) {
        BigInteger poolId = this.getPoolId(tokenA, tokenB);
        Map<String, Object> poolData = getPoolStats(poolId);
        Address baseCurrency = (Address) poolData.get("base_token");
        BigInteger price = (BigInteger) poolData.get("price");

        if (tokenA.equals(baseCurrency)) {
            return price;
         }
         else {
             return Utils.pow(BigInteger.TEN, BigInteger.valueOf(36)).divide(price);
         }
    }
}
