package arbitx.contracts;

import score.Context;
import score.VarDB;
import score.Address;
import score.annotation.External;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class Arbitx {

    // Contract name.
    private final String name;

    // Balanced contract addresses.
    private final VarDB<Address> sicx = Context.newVarDB("sICX", Address.class);
    private final VarDB<Address> bnusd = Context.newVarDB("bnUSD", Address.class);
    private final VarDB<Address> rebalancing = Context.newVarDB("rebalancing", Address.class);
    private final VarDB<Address> dex = Context.newVarDB("dex", Address.class);
    private final VarDB<Address> loan = Context.newVarDB("loan", Address.class);


    public Arbitx(String name) {
        this.name = name;
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
    public void liquidateUsers(List<Address> users) {
        for(int i = 0; i < users.size(); i++) {
            Context.call(this.loan.get(), "liquidate", users.get(i));
        }
        buyCollateralAtDiscount();    
    }

    @External(readonly = true)
    public List<Address> getLiquidableUsers () {
        Integer borrowerCount = (int) Context.call(this.loan.get(), "borrowerCount");  
        List<Address> liquidableUsers = new ArrayList<Address>();
        Address userAddress;
        String userStanding;

        for(int i=1; i <= borrowerCount; i++) {
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
                Context.call(loan, "retireBadDebt", "bnusd", bnusdBalance);
            }
            catch(Exception e){
                break;
            }

            sicxBalance = getTokenBalance(sicx);
            transferToken(sicx, dex, sicxBalance, createSwapData(bnusd));
        }   
    }

    private String getUserStanding(Address user) {
        String userData = (String) Context.call(this.loan.get(), "getAccountPosition", user);
        JsonObject json = Json.parse(userData).asObject();
        String standing = json.get("standing").asString();
        return standing;
    }






    //private Integer getBalancedLiquidationRatio() {
    //    String parameters = (String) Context.call(this.loan.get(), "getParameters");
    //    JsonObject json = Json.parse(parameters).asObject();
    //    Integer liquidationRatio = json.get("liquidation ratio").asInt();
    //    return liquidationRatio;
    //}

    //private static BigInteger pow10(int exponent) {
    //    BigInteger result = BigInteger.ONE;
    //    for (int i = 0; i < exponent; i++) {
    //        result = result.multiply(BigInteger.TEN);
    //    }
    //    return result;
    //}

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
