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

    // Contract name.
    private final String name;

    // Balanced contract addresses.
    private final VarDB<Address> sicx = Context.newVarDB("sICX", Address.class);
    private final VarDB<Address> bnusd = Context.newVarDB("bnUSD", Address.class);
    private final VarDB<Address> rebalancing = Context.newVarDB("rebalancing", Address.class);
    private final VarDB<Address> dex = Context.newVarDB("dex", Address.class);
    private final VarDB<Address> loan = Context.newVarDB("loan", Address.class);

    public BalancedLiquidation(String name) {
        this.name = name;
        this.loan.set(Address.fromString("cxae0fe2b1b4c3c224510b7168a2dd927791558493"));
    }

    @External(readonly = true)
    public String name() {
        return name;
    }

    @External
    public void setSicx (Address address) {
        Context.require(Context.getCaller() == Context.getOwner());
        this.sicx.set(address);
    }

    @External(readonly = true)
    public Address getSicx () {
        return this.sicx.get();
    }

    @External 
    public void setbnUSD (Address address) {
        Context.require(Context.getCaller() == Context.getOwner());
        this.bnusd.set(address);
    }

    @External(readonly = true)
    public Address getbnUSD () {
        return this.bnusd.get();
    }

    @External
    public void setRebalancing (Address address) {
        Context.require(Context.getCaller() == Context.getOwner());
        this.rebalancing.set(address);
    }

    @External(readonly = true)
    public Address getRebalancing () {
        return this.rebalancing.get();
    }

    @External
    public void setDex (Address address) {
        Context.require(Context.getCaller() == Context.getOwner());
        this.dex.set(address);
    }

    @External(readonly = true)
    public Address getDex () {
        return this.dex.get();
    }

    @External
    public void setLoan (Address address) {
        Context.require(Context.getCaller() == Context.getOwner());
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
