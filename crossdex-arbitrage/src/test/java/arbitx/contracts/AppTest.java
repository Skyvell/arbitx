package arbitx.contracts;

import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.Account;
import com.iconloop.score.token.irc2.IRC2Basic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import score.Address;
import score.Context;

import java.math.BigInteger;


class CrossdexArbitrageTest extends TestBase {

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    Score arbitx;

    public static class IRC2BasicToken extends IRC2Basic {
        public IRC2BasicToken(String _name, String _symbol, int _decimals, BigInteger _totalSupply) {
            super(_name, _symbol, _decimals);
            _mint(Context.getCaller(), _totalSupply);
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        arbitx = sm.deploy(owner, CrossdexArbitrage.class, "test");
        System.out.println("HEJ");
        System.out.flush();
    }


    @Test
    public void addPair() {
        String name = "sicx/bnusd";
        Address tokenA = Account.newScoreAccount(1).getAddress();
        Address tokenB = Account.newScoreAccount(2).getAddress();
        BigInteger convexusFee = BigInteger.valueOf(100);
        BigInteger arbitrageThreshold = BigInteger.valueOf(50);
        BigInteger tokensPerIteration = BigInteger.valueOf(100);

        arbitx.invoke(owner, "addPair", name, tokenA, tokenB, convexusFee, arbitrageThreshold, tokensPerIteration);

        

        Pair pair = (Pair) arbitx.call("getPair", name);

        System.out.println(pair);
        //System.out.println(pair.tokenA);
        System.out.flush();
    }

    @Test
    public void getPairs() {

    }

    @Test
    public void testy() {
        System.out.println("HEJ");
        System.out.flush();
    }
}