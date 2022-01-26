package arbitx.contracts;

import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.Account;
import com.iconloop.score.token.irc2.IRC2Basic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import score.Context;

import java.math.BigInteger;


class balancedStrategiesTest extends TestBase {

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
        arbitx = sm.deploy(owner, Arbitx.class, "test");
        System.out.println("HEJ");
        System.out.flush();
    }

    @Test
    public void testy() {
        System.out.println("HEJ");
        System.out.flush();
    }
}