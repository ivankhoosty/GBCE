package com.khoosty;

import com.khoosty.beans.DivInfoBean;
import com.khoosty.beans.TradeBean;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by ivan on 03/01/2017.
 */
public class CalcUtilsTest {
    @Test
    public void testGetAllShareIndex() {
        BigDecimal index = CalcUtils.getAllShareIndex(Collections.emptyList());
        Assert.assertTrue(BigDecimal.ZERO.compareTo(index) == 0);

        index = CalcUtils.getAllShareIndex(Arrays.asList(new BigDecimal(10), new BigDecimal(14.4)));
        Assert.assertTrue(new BigDecimal(12).compareTo(index) == 0);

        index = CalcUtils.getAllShareIndex(Arrays.asList(new BigDecimal(10), new BigDecimal(14.4), new BigDecimal(12)));
        Assert.assertTrue(new BigDecimal(12).compareTo(index) == 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllShareIndexZeroPrice() {
        BigDecimal index = CalcUtils.getAllShareIndex(Arrays.asList(new BigDecimal(10), new BigDecimal(0)));
        //should be unreachable
        Assert.assertNotNull(null);
    }

    @Test
    public void testGetDividendYieldCommon() {
        TradeBean tradeBean = TradeBean.createFromCSVString("POP,2017-01-03 23:00:10.525,10,S,10");
        DivInfoBean divInfoBean = DivInfoBean.createFromCSVString("POP,Common,8,0,100");

        BigDecimal dividendYield = CalcUtils.getDividendYield(tradeBean, divInfoBean);
        Assert.assertTrue(new BigDecimal("0.8").compareTo(dividendYield) == 0);
    }

    @Test
    public void testGetDividendYieldPreferred() {
        TradeBean tradeBean = TradeBean.createFromCSVString("GIN,2017-01-03 22:59:10.525,100,B,10");
        DivInfoBean divInfoBean = DivInfoBean.createFromCSVString("GIN,Preferred,8,2,100");

        BigDecimal dividendYield = CalcUtils.getDividendYield(tradeBean, divInfoBean);
        Assert.assertTrue(new BigDecimal("0.2").compareTo(dividendYield) == 0);
    }

    @Test
    public void testGetPERatio() {
        TradeBean tradeBean = TradeBean.createFromCSVString("POP,2017-01-03 23:00:10.525,10,S,10");
        DivInfoBean divInfoBean = DivInfoBean.createFromCSVString("POP,Common,8,0,100");

        BigDecimal PERatio = CalcUtils.getPERatio(tradeBean, divInfoBean);
        Assert.assertTrue(new BigDecimal("1.25").compareTo(PERatio) == 0);

        //test with 0 div, expecting null
        divInfoBean = DivInfoBean.createFromCSVString("POP,Common,0,0,100");

        PERatio = CalcUtils.getPERatio(tradeBean, divInfoBean);
        Assert.assertNull(PERatio);

        //test null divInfoBean
        PERatio = CalcUtils.getPERatio(tradeBean, null);
        Assert.assertNull(PERatio);
    }

    @Test
    public void testGetVolumeWeightedPrice(){
        List<TradeBean> tradeBeanList = Arrays.asList(
                TradeBean.createFromCSVString("TEA,2017-01-03 22:58:30.12,150,B,14.4"),
                TradeBean.createFromCSVString("TEA,2017-01-03 22:57:10.525,100,B,35"),
                TradeBean.createFromCSVString("TEA,2017-01-03 22:00:10.525,100,B,35") // this one should be ignored, over 15 mins
        );

        final long currentTimeMillis =  Timestamp.valueOf("2017-01-03 23:05:00.0").getTime();
        final Timestamp fromTimestamp = new Timestamp(currentTimeMillis - 15 * 60 * 1000);

        BigDecimal vwPrice = CalcUtils.getVolumeWeightedPrice(tradeBeanList, fromTimestamp);
        Assert.assertTrue(new BigDecimal("22.64").compareTo(vwPrice) == 0);
    }

}
