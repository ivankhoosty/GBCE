package com.khoosty;

import com.khoosty.beans.DivInfoBean;
import com.khoosty.beans.TradeBean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by ivan on 03/01/2017.
 */
public class CalcUtils {

    private static final int PRECISION = 4;
    private static MathContext mc = new MathContext(PRECISION, RoundingMode.HALF_UP);

    /**
     * Calculates Dividend Yield
     * @param tradeBean, expecting price to be positive decimal
     * @param divInfoBean, if null, assume no divinfo for symbol exists
     * @return For Common type - lastDividend/price; for Preferred type - (fixedDivdidend / 100 )* parValue / price; null if divInfoBean is null
     */
    public static BigDecimal getDividendYield(TradeBean tradeBean, DivInfoBean divInfoBean) {
        if (divInfoBean == null){
            //no divInfo available
            return null;
        }

        if (tradeBean.getPrice().compareTo(BigDecimal.ZERO) != 1) {
            throw new IllegalArgumentException("price must be positive decimal");
        }

        if (DivInfoBean.DivType.Preferred.equals(divInfoBean.getType())) {
            // (fixedDivdidend / 100 )* parValue / price
            return divInfoBean.getFixedDividendPercent().divide(new BigDecimal(100), mc)
                    .multiply(divInfoBean.getParValue())
                    .divide(tradeBean.getPrice(), mc);
        } else {
            //lastDividend / price
            return divInfoBean.getLastDividend().divide(tradeBean.getPrice(), mc);
        }
    }

    /**
     * Computes P/E Ratio
     * @param tradeBean
     * @param divInfoBean, if null, assume no divinfo for symbol exists
     * @return if dividend is 0, P/E Ratio cannot be computed, return null; otherwise it's price/dividend
     */
    public static BigDecimal getPERatio(TradeBean tradeBean, DivInfoBean divInfoBean) {
        if (divInfoBean == null){
            //no divInfo available
            return null;
        }

        if (divInfoBean.getLastDividend().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        // price / dividend
        return tradeBean.getPrice().divide(divInfoBean.getLastDividend(), mc);
    }

    /**
     * Computes Volume Weighted Price: sum(price_i * quantity_i)/sum(quantity_i)
     * @param tradeBeanList list of tradeBeans(sorted on input by timestamp desc)
     * @param from          cut off from which time the prices are to be used
     * @return null, if no matching tradeBeans found, computed volume weighted price otherwise
     */
    public static BigDecimal getVolumeWeightedPrice(List<TradeBean> tradeBeanList, Timestamp from) {
        BigDecimal priceQuantitySum = BigDecimal.ZERO; // sum (price * quantity)
        BigInteger quantitySum = BigInteger.ZERO; // sum (quantity)

        for (TradeBean tradeBean : tradeBeanList) {
            if (tradeBean.getTimestamp().after(from)) {
                if (tradeBean.getQuantity().compareTo(BigInteger.ZERO) != 1) {
                    throw new IllegalArgumentException("quantity must be positive integer");
                }

                priceQuantitySum = priceQuantitySum.add(tradeBean.getPrice().multiply(new BigDecimal(tradeBean.getQuantity())));
                quantitySum = quantitySum.add(tradeBean.getQuantity());
            } else {
                break; // as list is sorted in desc order
            }
        }

        if (quantitySum.compareTo(BigInteger.ZERO) == 0) {
            //no matching tradeBeans found within timeframe
            return null;
        } else {
            return priceQuantitySum.divide(new BigDecimal(quantitySum), mc);
        }
    }

    /**
     * Computes All Share Index as geometric mean of prices.
     * NOTE: BigDecimal doesn't have power function for decimal exponent, have to revert to Math.pow
     * @param priceList - prices for index computation
     * @return 0 if priceList is empty, computed index otherwise
     */
    public static BigDecimal getAllShareIndex(List<BigDecimal> priceList) {
        if (priceList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal allShareIndex = BigDecimal.ONE;
        for (BigDecimal price : priceList) {
            if (price.compareTo(BigDecimal.ZERO) != 1) {
                throw new IllegalArgumentException("price must be positive decimal");
            }

            //simplification in math : pow(a*b, n) is the same as pow(a, n) * pow(b, n); stops a*b blowing max_double
            allShareIndex = allShareIndex.multiply(new BigDecimal(Math.pow(price.doubleValue(), 1d / priceList.size())));
        }

        return allShareIndex.round(mc);
    }
}
