package com.khoosty;

import com.khoosty.beans.DivInfoBean;
import com.khoosty.beans.TradeBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by ivan on 03/01/2017.
 */

/**
 * Assumptions: single thread code
 */
public class Runner {
    private static Logger logger = LoggerFactory.getLogger(Runner.class);

    private static final String COMMENT_PREFIX = "#";
    public static final String CSV_DELIM = ",";

    public static void main(String args[]) {
        final Map<String, DivInfoBean> divInfoBeanMap;
        final Map<String, List<TradeBean>> tradeBeanMap;

        try {
            File divInfoFile = new File(Runner.class.getClassLoader().getResource("div_info.csv").getFile());
            divInfoBeanMap = parseDivInfo(divInfoFile);

            File tradesFile = new File(Runner.class.getClassLoader().getResource("trades.csv").getFile());
            tradeBeanMap = parseTrades(tradesFile);
        } catch (IOException e) {
            logger.error("unable to parse inputs", e);
            System.exit(-1);
            return;
        }

        //15 minute cut off timestamp, should be the same for all stocks
//        final long currentTimeMillis =  Timestamp.valueOf("2017-01-03 23:05:00.0").getTime();
        final long currentTimeMillis = System.currentTimeMillis();
        final Timestamp fromTimestamp = new Timestamp(currentTimeMillis - 15 * 60 * 1000);

        //list of last price for each symbol, to be used for index calculation
        final List<BigDecimal> priceList = new ArrayList<>(tradeBeanMap.size());

        for (String symbol : tradeBeanMap.keySet()) {
            List<TradeBean> tradeBeanList = tradeBeanMap.get(symbol);

            //we are interested in the last price only, which is always the first item in the list
            if (!tradeBeanList.isEmpty()) {
                priceList.add(tradeBeanList.get(0).getPrice());
            }

            for (TradeBean tradeBean : tradeBeanList) {
                logger.info("For {} :", tradeBean);
                DivInfoBean divInfoBean = divInfoBeanMap.get(symbol);
                BigDecimal dividendYield = CalcUtils.getDividendYield(tradeBean, divInfoBean);
                logger.info("Dividend Yield is [{}]", dividendYield);
                BigDecimal PERatio = CalcUtils.getPERatio(tradeBean, divInfoBean);
                logger.info("P/E Ratio is [{}]", PERatio);
            }
            BigDecimal vwPrice = CalcUtils.getVolumeWeightedPrice(tradeBeanList, fromTimestamp);
            logger.info("Volume Weighted Price is [{}]", vwPrice);
            logger.info("---");
        }

        BigDecimal allShareIndex = CalcUtils.getAllShareIndex(priceList);
        logger.info("All Share Index is [{}]", allShareIndex);
    }

    /**
     * Parse input file for div info, any failed rows are ignored with error logged
     *
     * @param f input file
     * @return Map of stock symbols to divInfo bean
     * @throws IOException
     */
    private static Map<String, DivInfoBean> parseDivInfo(File f) throws IOException {
        Map<String, DivInfoBean> divInfoBeanMap = new HashMap<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.isBlank(line) || line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                try {
                    DivInfoBean divInfoBean = DivInfoBean.createFromCSVString(line);
                    DivInfoBean previousValue = divInfoBeanMap.put(divInfoBean.getSymbol(), divInfoBean);
                    if (previousValue != null) {
                        logger.warn("replaced div info [{}] with [{}]", previousValue, divInfoBean);
                    }
                    logger.info("read [{}]", divInfoBean);
                } catch (IllegalArgumentException iae) {
                    logger.error("input error: ", iae);
                }
            }
        }

        return divInfoBeanMap;
    }

    /**
     * Parse input file for trade data, any failed rows are ignored with error logged
     *
     * @param f input file
     * @return Map of Stock Symbol to List of trades for that symbol, ordered by timestamp desc
     * @throws IOException
     */
    private static Map<String, List<TradeBean>> parseTrades(File f) throws IOException {
        Map<String, List<TradeBean>> tradeBeanMap = new HashMap<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.isBlank(line) || line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                try {
                    TradeBean tradeBean = TradeBean.createFromCSVString(line);
                    //choice of LinkedList is deliberate over ArrayList, as I dont know the size of the input file
                    List<TradeBean> tradeBeanList = tradeBeanMap.computeIfAbsent(tradeBean.getSymbol(), k -> new LinkedList<>());
                    tradeBeanList.add(tradeBean);

                    logger.info("read [{}]", tradeBean);
                } catch (IllegalArgumentException iae) {
                    logger.error("input error: ", iae);
                }
            }
        }

        for (List<TradeBean> tradeBeanList : tradeBeanMap.values()) {
            //sort each list by timestamp
            tradeBeanList.sort(TradeBean.timestampDescComparator);
        }
        return tradeBeanMap;
    }
}
