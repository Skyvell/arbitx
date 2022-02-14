package arbitx.contracts;

import score.Context;
import score.VarDB;
import score.Address;
import score.annotation.External;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.math.BigInteger;


public class TradeExecutor {
    private final String name;

    private final VarDB<Address> balancedDex = Context.newVarDB("balanced_dex", Address.class);
    private final VarDB<Address> convexusDex = Context.newVarDB("convexus_dex", Address.class);
    private final VarDB<Address> arbitrageSettings = Context.newVarDB("arbitrage_settings", Address.class);

    // Current ongoing arbitrage.
    private final VarDB<BigInteger> initialTokenAmount = Context.newVarDB("amount_to_sell", BigInteger.class);
    private final VarDB<Address> currentToken = Context.newVarDB("receive_address", Address.class);

    private final EnumerableMap<String, Pair> pairs = new EnumerableMap("pairs", String.class, Pair.class);

    public TradeExecutor(String name) {
        this.name = name;
    }

    @External(readonly = true)
    public String name() {
        return name;
    }

    @External
    public void executeArbitrage(Address buyExchange, Address sellExchange, Address token, BigInteger amount) {
        Context.require(Context.getCaller() == this.arbitrageSettings.get());
        this.initialTokenAmount.set(amount);
        this.currentToken.set(token);
        Address convexusDex = this.convexusDex.get();
        Address balancedDex = this.balancedDex.get();
        
        if (buyExchange == convexusDex) {
            // Buy tokens from Convexus.
            // Sell tokens to Balanced.
        }
        else {
            // Buy tokens from Balanced.
            // Sell tokens to Convexus.
        }
    }

    @External
    public void tokenFallback (Address from, BigInteger value, byte[] data) {
        if (from != currentToken) {
            return;
        }

        if (value.compareTo(this.initialTokenAmount.get()) < 0) {
            Context.revert("Arbitrage not profitable, rolling back.");
        }

        this.currentToken.set(null);
    }

    @External(readonly = true)
    public BigInteger getTokenBalance(Address token) {
        return (BigInteger) Context.call(token, "balanceOf", Context.getAddress());
    }

    private void transferToken(Address token, Address to, BigInteger amount, byte[] data) {
        Context.call(token, "transfer", to, amount, data);
    }

    private byte[] createBalancedSwapData(Address toToken) {
        JsonObject data = Json.object();
        data.add("method", "_swap");
        data.add("params", Json.object().add("toToken", toToken.toString()));
        return data.toString().getBytes();
    }
}
