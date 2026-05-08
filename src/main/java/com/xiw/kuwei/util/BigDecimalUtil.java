package com.xiw.kuwei.util;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalUtil {

    public static final BigDecimal BD_100 = new BigDecimal("100");


    private static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;

    public static BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        return pow(base, exponent, DEFAULT_MATH_CONTEXT);
    }

    public static BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mc) {
        BigDecimal ln = ln(base, mc);
        return exp(exponent.multiply(ln, mc), mc);
    }

    public static BigDecimal ln(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("x must be > 0");
        }

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term;
        BigDecimal y = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), mc);

        BigDecimal y2 = y.multiply(y, mc);
        term = y;

        for (int i = 1; i < 50; i += 2) {
            result = result.add(term.divide(BigDecimal.valueOf(i), mc), mc);
            term = term.multiply(y2, mc);
        }

        return result.multiply(BigDecimal.valueOf(2), mc);
    }

    public static BigDecimal exp(BigDecimal x, MathContext mc) {
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;

        for (int i = 1; i < 50; i++) {
            term = term.multiply(x, mc)
                    .divide(BigDecimal.valueOf(i), mc);
            result = result.add(term, mc);
        }

        return result;
    }

}
