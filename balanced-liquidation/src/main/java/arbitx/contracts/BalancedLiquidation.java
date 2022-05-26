package arbitx.contracts;

import score.Context;
import score.VarDB;
import score.Address;
import score.annotation.External;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import scorex.util.ArrayList;


public class BalancedLiquidation {
    private final String name;

    private final VarDB<Address> operator = Context.newVarDB("sICX", Address.class);

    private final VarDB<Address> sicx = Context.newVarDB("sICX", Address.class);
    private final VarDB<Address> bnusd = Context.newVarDB("bnUSD", Address.class);
    private final VarDB<Address> rebalancing = Context.newVarDB("rebalancing", Address.class);
    private final VarDB<Address> dex = Context.newVarDB("dex", Address.class);
    private final VarDB<Address> loan = Context.newVarDB("loan", Address.class);

    public BalancedLiquidation(String name) {
        this.name = name;
        this.loan.set(Address.fromString("cx66d4d90f5f113eba575bf793570135f9b10cece1"));
        this.dex.set(Address.fromString("cxa0af3165c08318e988cb30993b3048335b94af6c"));
        this.rebalancing.set(Address.fromString("cx40d59439571299bca40362db2a7d8cae5b0b30b0"));
        this.operator.set(Address.fromString("hx40f44f65b0a84dc1a608518ae585d3863b34d6a2")); 
        this.bnusd.set(Address.fromString("cx88fd7df7ddff82f7cc735c871dc519838cb235bb"));
        this.sicx.set(Address.fromString("cx2609b924e33ef00b648a409245c7ea394c467824"));
        
    }

    @External(readonly = true)
    public String name() {
        return name;
    }

    @External
    public void setSicx (Address address) {
        Context.require(Context.getCaller() == this.operator.get());
        this.sicx.set(address);
    }

    @External(readonly = true)
    public Address getSicx () {
        return this.sicx.get();
    }

    @External 
    public void setbnUSD (Address address) {
        Context.require(Context.getCaller() == this.operator.get());
        this.bnusd.set(address);
    }

    @External(readonly = true)
    public Address getbnUSD () {
        return this.bnusd.get();
    }

    @External
    public void setRebalancing (Address address) {
        Context.require(Context.getCaller() == this.operator.get());
        this.rebalancing.set(address);
    }

    @External(readonly = true)
    public Address getRebalancing () {
        return this.rebalancing.get();
    }

    @External
    public void setDex (Address address) {
        Context.require(Context.getCaller() == this.operator.get());
        this.dex.set(address);
    }

    @External(readonly = true)
    public Address getDex () {
        return this.dex.get();
    }

    @External
    public void setLoan (Address address) {
        Context.require(Context.getCaller() == this.operator.get());
        this.loan.set(address);
    }

    @External(readonly = true)
    public Address getLoan () {
        return this.loan.get();
    }

    @External
    public void operateOnUsers(Address[] users) {
        for(int i = 0; i < users.length; i++) {
            Context.call(this.loan.get(), "liquidate", users[i]);
        }

        try {
            buyCollateralAtDiscount();    
        }
        catch(Exception e) {
            return;
        }
    }

    @External(readonly = true)
    public List<Address> getLiquidableUsers () {
        BigInteger borrowerCount = (BigInteger) Context.call(this.loan.get(), "borrowerCount");  
        ArrayList<Address> liquidableUsers = new ArrayList<Address>();
        Address userAddress;
        String userStanding;

        for(BigInteger i = BigInteger.ONE; i.compareTo(borrowerCount) <= 0; i=i.add(BigInteger.ONE)) {
            userAddress = (Address) Context.call(this.loan.get(), "getPositionAddress", i);
            userStanding = getUserStanding(userAddress);
            
            if(userStanding.equals("Liquidate")) {
                liquidableUsers.add(userAddress);
            }  
        }
        return liquidableUsers;
    }

    @External
    public void buyCollateralAtDiscount() {
        Address loan = this.loan.get();
        Address dex = this.dex.get();
        Address sicx = this.sicx.get();
        Address bnusd = this.bnusd.get();

        BigInteger bnusdBalance;
        BigInteger sicxBalance;

        sicxBalance = getTokenBalance(sicx);
        transferToken(sicx, dex, sicxBalance, createSwapData(bnusd));
        
        while(true) {
            bnusdBalance = getTokenBalance(bnusd);

            try {
                Context.call(loan, "retireBadDebt", "bnUSD", bnusdBalance);
            }
            catch(Exception e){
                break;
            }

            sicxBalance = getTokenBalance(sicx);
            Context.call(this.rebalancing.get(), "rebalance");
            transferToken(sicx, dex, sicxBalance, createSwapData(bnusd));
        }   
    }

    @External(readonly = true)
    public String getUserStanding(Address user) {
        Map<String, Object> userData = (Map<String, Object>) Context.call(this.loan.get(), "getAccountPositions", user);
        String standing = userData.get("standing").toString();
        return standing;
    }

    @External(readonly = true)
    public BigInteger getBorrowerCount() {
        return (BigInteger) Context.call(this.loan.get(), "borrowerCount");
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
