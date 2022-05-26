package arbitx.contracts;

import java.math.BigInteger;

import score.Address;

public class Constants {
    public static final Address SICX_ADDRESS = Address.fromString("cx2609b924e33ef00b648a409245c7ea394c467824");
    public static final Address BNUSD_ADDRESS = Address.fromString("cx88fd7df7ddff82f7cc735c871dc519838cb235bb");
    public static final Address DEX_ADDRESS = Address.fromString("cxa0af3165c08318e988cb30993b3048335b94af6c");
    public static final BigInteger EXA = Utils.pow(BigInteger.TEN, BigInteger.valueOf(18));
    public static final BigInteger BASISPOINTS = Utils.pow(BigInteger.TEN, BigInteger.valueOf(4));
}
