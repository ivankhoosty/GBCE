package com.khoosty.beans;

import java.math.BigDecimal;
import java.util.StringTokenizer;

import static com.khoosty.Runner.CSV_DELIM;

/**
 * Created by ivan on 03/01/2017.
 */
public class DivInfoBean {
    public enum DivType {
        Common, Preferred;
    }

    private String symbol;
    private DivType type;
    private BigDecimal lastDividend;
    private BigDecimal fixedDividendPercent;
    private BigDecimal parValue;

    public DivInfoBean(String symbol, DivType type, BigDecimal lastDividend, BigDecimal fixedDividendPercent, BigDecimal parValue) {
        this.symbol = symbol;
        this.type = type;
        this.lastDividend = lastDividend;
        this.fixedDividendPercent = fixedDividendPercent;
        this.parValue = parValue;
    }

    /**
     * parses CSV string to div info bean, ANY failures result in IllegalArgumentException
     * Assumptions: all string values are case sensitive
     */
    public static DivInfoBean createFromCSVString(String s) throws IllegalArgumentException {
        try {
            StringTokenizer st = new StringTokenizer(s, CSV_DELIM);

            String symbol = st.nextToken();
            DivType type = DivType.valueOf(st.nextToken());
            BigDecimal lastDividend = new BigDecimal(st.nextToken());
            BigDecimal fixedDividend = new BigDecimal(st.nextToken());
            BigDecimal parValue = new BigDecimal(st.nextToken());

            if (st.hasMoreTokens()) {
                throw new IllegalArgumentException("extra values found");
            }

            return new DivInfoBean(symbol, type, lastDividend, fixedDividend, parValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("[" + s + "] is not parsable", e);
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public DivType getType() {
        return type;
    }

    public BigDecimal getLastDividend() {
        return lastDividend;
    }

    public BigDecimal getFixedDividendPercent() {
        return fixedDividendPercent;
    }

    public BigDecimal getParValue() {
        return parValue;
    }

    @Override
    public String toString() {
        return "DivInfoBean{" +
                "symbol='" + symbol + '\'' +
                ", type=" + type +
                ", lastDividend=" + lastDividend +
                ", fixedDividendPercent=" + fixedDividendPercent +
                ", parValue=" + parValue +
                '}';
    }
}