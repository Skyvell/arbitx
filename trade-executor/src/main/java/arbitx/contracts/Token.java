package arbitx.contracts;

import java.math.BigInteger;

import score.Address;
import score.Context;

public class Token {
    public String name;
    public Address address;
    public BigInteger balance;

    public Token(Address tokenAddress) {
        this.address = tokenAddress;
        this.name = this.getTokenName();
        this.balance = this.getTokenBalance();
    }

    private BigInteger getTokenBalance() {
        return (BigInteger) Context.call(this.address, "balanceOf", Context.getAddress());
    }

    private String getTokenName() {
        return (String) Context.call(this.address, "name");
    }
}
