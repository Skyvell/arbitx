package arbitx.contracts;

import score.Context;
import score.VarDB;
import score.Address;
import score.annotation.External;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.math.BigInteger;


public class CrossdexArbitrage {
    private final String name;
    private final VarDB<Address> balancedDex = Context.newVarDB("balanced_dex", Address.class);
    private final VarDB<Address> convexusDex = Context.newVarDB("convexus_dex", Address.class);
    private final EnumerableMap<String, Pair> pairs = new EnumerableMap("pairs", String.class, Pair.class);

    public CrossdexArbitrage(String name) {
        this.name = name;
    }

    @External(readonly = true)
    public String name() {
        return name;
    }

    public void addPair() {

    }

    public void removePair() {
        
    }

    @External
    public void tokenFallback (Address from, BigInteger value, byte[] data) {
        return;
    }

    @External(readonly = true)
    public BigInteger getTokenBalance(Address token) {
        return (BigInteger) Context.call(token, "balanceOf", Context.getAddress());
    }

    private void transferToken(Address token, Address to, BigInteger amount, byte[] data) {
        Context.call(token, "transfer", to, amount, data);
    }

    private byte[] createSwapData(Address toToken) {
        JsonObject data = Json.object();
        data.add("method", "_swap");
        data.add("params", Json.object().add("toToken", toToken.toString()));
        return data.toString().getBytes();
    }
}
