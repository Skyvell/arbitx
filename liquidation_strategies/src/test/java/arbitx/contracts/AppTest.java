package arbitx.contracts;

import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.Account;
import com.iconloop.score.token.irc2.IRC2Basic;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import arbitx.contracts.Arbitx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import score.Context;

import java.math.BigInteger;


class balancedStrategiesTest extends TestBase {

    private static final ServiceManager sm = getServiceManager();

    private static final Account owner = sm.createAccount();

    private static Score balancedStrategies;
    private static Score sicx;
    private static Score bnusd;

    // balancedStrategies score deployment settings.
    private static final String nameBalancedStragegies = "Balanced Strategies";

    // Sicx score deployment settings.
    private static final String nameSicx = "Staked icx";
    private static final String symbolSicx = "SICX";
    private static final int decimalsSicx = 18;
    private static final BigInteger initalsupplySicx = BigInteger.valueOf(100);

    // Bnusd score deployment settings.
    private static final String nameBnusd = "Balanced usd";
    private static final String symbolBnusd = "BNUSD";
    private static final int decimalsBnusd = 18;
    private static final BigInteger initalsupplyBnusd = BigInteger.valueOf(100);

    public static class IRC2BasicToken extends IRC2Basic {
        public IRC2BasicToken(String _name, String _symbol, int _decimals, BigInteger _totalSupply) {
            super(_name, _symbol, _decimals);
            _mint(Context.getCaller(), _totalSupply);
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        balancedStrategies = sm.deploy(owner, Arbitx.class, nameBalancedStragegies);
        sicx = sm.deploy(owner, IRC2BasicToken.class, nameSicx, symbolSicx, decimalsSicx, initalsupplySicx);
        bnusd = sm.deploy(owner, IRC2BasicToken.class, nameBnusd, symbolBnusd, decimalsBnusd, initalsupplyBnusd);
    }

    @Test
    void name() {
        assertEquals(nameBalancedStragegies, balancedStrategies.call("name"));
    }

    @Test
    void setGetSicx() {
        assertNull(balancedStrategies.call("getSicx"));
        Account sicx = sm.createAccount();

        balancedStrategies.invoke(owner, "setSicx", sicx.getAddress());

        assertEquals(sicx.getAddress(), balancedStrategies.call("getSicx"));
    }

    @Test
    void setGetRebalancing() {
        assertNull(balancedStrategies.call("getRebalancing"));
        Account rebalancing = sm.createAccount();

        balancedStrategies.invoke(owner, "setRebalancing", rebalancing.getAddress());

        assertEquals(rebalancing.getAddress(), balancedStrategies.call("getRebalancing"));
    }

    @Test
    void setGetDex() {
        assertNull(balancedStrategies.call("getDex"));
        Account dex = sm.createAccount();

        balancedStrategies.invoke(owner, "setDex", dex.getAddress());

        assertEquals(dex.getAddress(), balancedStrategies.call("getDex"));
    }

    @Test
    void setGetBnusd() {
        assertNull(balancedStrategies.call("getbnUSD"));
        Account bnusd = sm.createAccount();

        balancedStrategies.invoke(owner, "setbnUSD", bnusd.getAddress());

        assertEquals(bnusd.getAddress(), balancedStrategies.call("getbnUSD"));
    }

    @Test
    void getbalancedStrategiesBalance () {
        // Set required addresses in balancedStrategies contract.
        balancedStrategies.invoke(owner, "setSicx", sicx.getAddress());
        balancedStrategies.invoke(owner, "setbnUSD", bnusd.getAddress());

        // Set mock balances for balancedStrategies contract.
        Account balancedStrategiesAccount = balancedStrategies.getAccount();
        balancedStrategiesAccount.addBalance("sicx", BigInteger.valueOf(67));
        balancedStrategiesAccount.addBalance("bnusd", BigInteger.valueOf(65));

        // Transfer sicx and bnusd to balancedStrategies.
        sicx.invoke(owner, "transfer", balancedStrategies.getAddress(), balancedStrategiesAccount.getBalance("sicx"), new byte[0]);
        bnusd.invoke(owner, "transfer", balancedStrategies.getAddress(), balancedStrategiesAccount.getBalance("bnusd"), new byte[0]);
        
        // Act.
        String balance = (String) balancedStrategies.call("getbalancedStrategiesBalance");
        JsonObject json = Json.parse(balance).asObject();

        // Assert.
        assertEquals(balancedStrategiesAccount.getBalance("sicx"), new BigInteger(json.get("sicx").asString()));
        assertEquals(balancedStrategiesAccount.getBalance("bnusd"), new BigInteger(json.get("bnusd").asString()));
    }
}