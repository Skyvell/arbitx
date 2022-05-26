package arbitx.contracts;

import java.math.BigInteger;


public class Utils {
    
    public static final BigInteger pow(BigInteger base, BigInteger exponent) {
        BigInteger result = base;
        BigInteger iterations = exponent.subtract(BigInteger.ONE);
        for (BigInteger i = BigInteger.ZERO; i.compareTo(iterations) < 0; i = i.add(BigInteger.ONE)) {
            result = result.multiply(base);
        }
        return result;
    }

    public static final BigInteger sqrtPriceX96ToPrice(BigInteger sqrtPriceX96) {
        return (pow(sqrtPriceX96, BigInteger.TWO).multiply(Constants.EXA)).divide(pow(BigInteger.TWO, BigInteger.valueOf(192)));
    }
}
