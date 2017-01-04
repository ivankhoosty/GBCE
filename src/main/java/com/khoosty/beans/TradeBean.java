package com.khoosty.beans;

import org.junit.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.StringTokenizer;

import static com.khoosty.Runner.CSV_DELIM;

/**
 * Created by ivan on 03/01/2017.
 */
public class TradeBean {
    public enum Indicator {
        B, S;
    }

    private String symbol;
    private Timestamp timestamp;
    private BigInteger quantity;
    private Indicator indicator;
    private BigDecimal price;

    public TradeBean(String symbol, Timestamp timestamp, BigInteger quantity, Indicator indicator, BigDecimal price) {
        if (quantity.compareTo(BigInteger.ZERO) != 1) {
            throw new IllegalArgumentException("quantity must be positive integer");
        }

        if (price.compareTo(BigDecimal.ZERO) != 1) {
            throw new IllegalArgumentException("price must be positive decimal");
        }

        this.symbol = symbol;
        this.timestamp = timestamp;
        this.quantity = quantity;
        this.indicator = indicator;
        this.price = price;
    }

    /**
     * parses CSV string to trade bean, ANY failures result in IllegalArgumentException
     * Assumptions: all string values are case sensitive; timezone is default of jvm
     */
    public static TradeBean createFromCSVString(String s) throws IllegalArgumentException {
        try {
            StringTokenizer st = new StringTokenizer(s, CSV_DELIM);

            String symbol = st.nextToken();
            Timestamp timestamp = Timestamp.valueOf(st.nextToken());
            BigInteger quantity = new BigInteger(st.nextToken());
            Indicator indicator = Indicator.valueOf(st.nextToken());
            BigDecimal price = new BigDecimal(st.nextToken());

            if (st.hasMoreTokens()) {
                throw new IllegalArgumentException("extra values found");
            }

            return new TradeBean(symbol, timestamp, quantity, indicator, price);
        } catch (Exception e) {
            throw new IllegalArgumentException("[" + s + "] is not parsable", e);
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public BigInteger getQuantity() {
        return quantity;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "TradeBean{" +
                "symbol='" + symbol + '\'' +
                ", timestamp=" + timestamp +
                ", quantity=" + quantity +
                ", indicator=" + indicator +
                ", price=" + price +
                '}';
    }

    public static final Comparator<TradeBean> timestampDescComparator = (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp());
}
