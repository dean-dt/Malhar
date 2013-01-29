/*
 *  Copyright (c) 2012-2013 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.demos.yahoofinance;

import com.malhartech.api.ApplicationFactory;
import com.malhartech.api.DAG;
import com.malhartech.lib.io.ConsoleOutputOperator;
import com.malhartech.lib.math.AverageKeyVal;
import com.malhartech.lib.math.SumKeyVal;
import com.malhartech.lib.multiwindow.MultiWindowRangeKeyVal;
import com.malhartech.lib.multiwindow.MultiWindowSumKeyVal;
import com.malhartech.lib.multiwindow.SimpleMovingAverage;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *       Yahoo finance application demo. <p>
 *
 *       Get Yahoo finance feed and calculate minute price range, minute volume, simple moving average of 5 minutes.
 *
 *       @author Locknath Shil <locknath@malhar-inc.com>
 */
public class YahooFinanceApplication implements ApplicationFactory
{
  private static final Logger logger = LoggerFactory.getLogger(YahooFinanceApplication.class);

  /**
   *       Get actual Yahoo finance ticks of symbol, last price, total daily volume, and last traded price.
   *
   *       @param name
   *       @param dag
   *       @return inputs
   */
  public StockTickInput getTicks(String name, DAG dag, boolean dummy, boolean logTime)
  {
    StockTickInput oper = dag.addOperator(name, StockTickInput.class);
    oper.setReadIntervalMillis(200);
    oper.setLogTime(logTime);
    //oper.addSymbol("YHOO");
    //oper.addSymbol("EBAY");
    oper.addSymbol("AAPL");
   // oper.addSymbol("GOOG");
    oper.setIsDummy(dummy);
    return oper;
  }

  /**
   *       This sends total daily volume by adding volumes from each ticks.
   *
   *       @param name
   *       @param dag
   *       @return daily volume
   */
  public SumKeyVal<String, Long> getDailyVolume(String name, DAG dag)
  {
    SumKeyVal<String, Long> oper = dag.addOperator(name, SumKeyVal.class);
    oper.setType(Long.class);
    //oper.setResetAtEndWindow(false);
    return oper;
  }

  /**
   *       Get cumulative volume of 1 minute and send at the end window of 1 minute.
   *
   *       @param name
   *       @param dag
   *       @param appWindow
   *       @return minute volume
   */
  public MultiWindowSumKeyVal<String, Long> getTimedVolume(String name, DAG dag, int appWindow)
  {
    MultiWindowSumKeyVal<String, Long> oper = dag.addOperator(name, MultiWindowSumKeyVal.class);
    oper.setType(Long.class);
    oper.setWindowSize(appWindow);
    return oper;
  }

  /**
   *       Get High-low range for 1 minute.
   *
   *       @param name
   *       @param dag
   *       @param appWindow
   *       @return high-low range of 1 minute
   */
  public MultiWindowRangeKeyVal<String, Double> getTimedPriceRange(String name, DAG dag, int appWindow)
  {
    MultiWindowRangeKeyVal<String, Double> oper = dag.addOperator(name, MultiWindowRangeKeyVal.class);
    oper.setType(Double.class);
    oper.setWindowSize(appWindow);
    return oper;
  }

  /**
   *       Merge price, daily volume, time.
   *
   *       @param name
   *       @param dag
   *       @return merge of price, daily volume, and time
   */
  public PriceVolumeConsolidator getPriceVolumeConsolidator(String name, DAG dag)
  {
    PriceVolumeConsolidator oper = dag.addOperator(name, PriceVolumeConsolidator.class);
    return oper;
  }

  /**
   *      Merge minute volume and minute high-low.
   *
   *      @param name
   *      @param dag
   *      @return merge of minute volume and price range
   */
  public RangeVolumeConsolidator getRangeVolumeConsolidator(String name, DAG dag)
  {
    RangeVolumeConsolidator oper = dag.addOperator(name, RangeVolumeConsolidator.class);
    return oper;
  }

  /**
   *      Merge SMA of price and volume.
   *
   *      @param name
   *      @param dag
   *      @return merge sma of price and volume
   */
  public SMAConsolidator getSMAConsolidator(String name, DAG dag)
  {
    SMAConsolidator oper = dag.addOperator(name, SMAConsolidator.class);
    return oper;
  }

  /**
   *      Get average price of streaming window.
   *
   *      @param name
   *      @param dag
   *      @return average price of streaming window
   */
  public AverageKeyVal<String, Double> getPriceAverage(String name, DAG dag)
  {
    AverageKeyVal<String, Double> oper = dag.addOperator(name, AverageKeyVal.class);
    return oper;
  }

  /**
   *      Get average volume of streaming window.
   *
   *      @param name
   *      @param dag
   *      @return average volume of streaming window
   */
  public AverageKeyVal<String, Long> getVolumeAverage(String name, DAG dag)
  {
    AverageKeyVal<String, Long> oper = dag.addOperator(name, AverageKeyVal.class);
    return oper;
  }

  /**
   *      Get simple moving average of price.
   *
   *      @param name
   *      @param dag
   *      @param appWindow
   *      @return simple moving average of price
   */
  public SimpleMovingAverage getPriceSimpleMovingAverage(String name, DAG dag, int appWindow)
  {
    SimpleMovingAverage oper = dag.addOperator(name, SimpleMovingAverage.class);
    oper.setWindowSize(appWindow);
    oper.setType(Double.class);
    return oper;
  }

  /**
   *      Get simple moving average of volume.
   *
   *      @param name
   *      @param dag
   *      @param appWindow
   *      @return simple moving average of price
   */
  public SimpleMovingAverage getVolumeSimpleMovingAverage(String name, DAG dag, int appWindow)
  {
    SimpleMovingAverage oper = dag.addOperator(name, SimpleMovingAverage.class);
    oper.setWindowSize(appWindow);
    oper.setType(Long.class);
    return oper;
  }

  /**
   *     Get console for simple moving average.
   *
   *     @param name
   *     @param dag
   *     @return priceSMA console
   */
  public ConsoleOutputOperator getConsole(String name, DAG dag)
  {
    ConsoleOutputOperator oper = dag.addOperator(name, ConsoleOutputOperator.class);
    return oper;
  }

  /**
   *     Create DAG
   *
   *     @param conf
   *     @return dag
   */
  @Override
  public DAG getApplication(Configuration conf)
  {
    DAG dag = new DAG(conf);
    int streamingWindowSize = 1000; // Default streaming window size is 500 msec. Set this to 1 sec.
    dag.getAttributes().attr(DAG.STRAM_WINDOW_SIZE_MILLIS).set(streamingWindowSize);
    boolean allInline = true;
    boolean isDummy = false;  // true for dummy data
    int winSize = 60;
    int appWindow = 300;
    boolean windowTest = true;
    boolean timeTest = false;
    boolean smaTest = false;
    boolean allTest = true;


    if (windowTest) {
      StockTickInput tick = getTicks("tick", dag, isDummy, true);
      SumKeyVal<String, Long> dailyVolume = getDailyVolume("dailyVolume", dag);
      PriceVolumeConsolidator pvConsolidator = getPriceVolumeConsolidator("priceVolumeMerger", dag);
      ConsoleOutputOperator windowedConsole = getConsole("windowedConsole", dag);

      dag.addStream("volume_tick", tick.volume, dailyVolume.data).setInline(allInline);
      dag.addStream("price_tick", tick.price, pvConsolidator.data1).setInline(allInline);
      dag.addStream("time_tick", tick.time, pvConsolidator.data3).setInline(allInline);
      dag.addStream("volume_pvConsolidator", dailyVolume.sum, pvConsolidator.data2).setInline(true);
      dag.addStream("pvConsolidator_console", pvConsolidator.out, windowedConsole.input).setInline(true);
    }
    else if (timeTest) {
      StockTickInput tick = getTicks("tick", dag, isDummy, false);
      MultiWindowRangeKeyVal<String, Double> highlow = getTimedPriceRange("highlow", dag, winSize);
      MultiWindowSumKeyVal<String, Long> minuteVolume = getTimedVolume("timedVolume", dag, winSize);
      RangeVolumeConsolidator rvConsolidator = getRangeVolumeConsolidator("con2", dag);
      ConsoleOutputOperator minuteConsole = getConsole("timedConsole", dag);

      dag.addStream("price_tick", tick.price, highlow.data).setInline(allInline);
      dag.addStream("volume_tick", tick.volume, minuteVolume.data).setInline(allInline);
      dag.addStream("highlow_merge", highlow.range, rvConsolidator.data1).setInline(true);
      dag.addStream("volume_merge", minuteVolume.sum, rvConsolidator.data2).setInline(true);
      dag.addStream("minute_console", rvConsolidator.out, minuteConsole.input).setInline(true);
    }
    else if (smaTest) {
      StockTickInput tick = getTicks("tick", dag, isDummy, false);
      AverageKeyVal<String, Double> priceAvg = getPriceAverage("priceAvg", dag);
      AverageKeyVal<String, Long> volumeAvg = getVolumeAverage("volumeAvg", dag);
      SimpleMovingAverage priceSMA = getPriceSimpleMovingAverage("smaPrice", dag, appWindow);
      SimpleMovingAverage volumeSMA = getVolumeSimpleMovingAverage("smaVolume", dag, appWindow);
      SMAConsolidator smaConsolidator = getSMAConsolidator("consolidator", dag);
      ConsoleOutputOperator smaConsole = getConsole("smaConsole", dag);

      dag.addStream("price_tick", tick.price, priceAvg.data).setInline(allInline);
      dag.addStream("priceAverage_priceSma", priceAvg.average, priceSMA.data).setInline(true);
      dag.addStream("priceSma_smaCconsolidator", priceSMA.doubleSMA, smaConsolidator.data1).setInline(true);
      dag.addStream("volume_tick", tick.volume, volumeAvg.data).setInline(allInline);
      dag.addStream("volumeAverage_volumeSma", volumeAvg.average, volumeSMA.data).setInline(true);
      dag.addStream("volumeSma_smaCconsolidator", volumeSMA.longSMA, smaConsolidator.data2).setInline(true);
      dag.addStream("smaConsolidator_console", smaConsolidator.out, smaConsole.input).setInline(true);
    }
    else if (allTest) {
      StockTickInput tick = getTicks("tick", dag, isDummy, true);
      SumKeyVal<String, Long> dailyVolume = getDailyVolume("dailyVolume", dag);
      PriceVolumeConsolidator pvConsolidator = getPriceVolumeConsolidator("priceVolumeMerger", dag);
      ConsoleOutputOperator windowedConsole = getConsole("windowedConsole", dag);
      MultiWindowRangeKeyVal<String, Double> highlow = getTimedPriceRange("highlow", dag, winSize);
      MultiWindowSumKeyVal<String, Long> minuteVolume = getTimedVolume("timedVolume", dag, winSize);
      RangeVolumeConsolidator rvConsolidator = getRangeVolumeConsolidator("con2", dag);
      ConsoleOutputOperator minuteConsole = getConsole("timedConsole", dag);
      AverageKeyVal<String, Double> priceAvg = getPriceAverage("priceAvg", dag);
      AverageKeyVal<String, Long> volumeAvg = getVolumeAverage("volumeAvg", dag);
      SimpleMovingAverage priceSMA = getPriceSimpleMovingAverage("smaPrice", dag, appWindow);
      SimpleMovingAverage volumeSMA = getVolumeSimpleMovingAverage("smaVolume", dag, appWindow);
      SMAConsolidator smaConsolidator = getSMAConsolidator("consolidator", dag);
      ConsoleOutputOperator smaConsole = getConsole("smaConsole", dag);

      dag.addStream("price_tick", tick.price, pvConsolidator.data1, highlow.data, priceAvg.data).setInline(allInline);
      dag.addStream("volume_tick", tick.volume, dailyVolume.data, minuteVolume.data, volumeAvg.data).setInline(allInline);
      dag.addStream("time_tick", tick.time, pvConsolidator.data3).setInline(allInline);

      dag.addStream("volume_pvConsolidator", dailyVolume.sum, pvConsolidator.data2).setInline(true);
      dag.addStream("pvConsolidator_console", pvConsolidator.out, windowedConsole.input).setInline(true);

      dag.addStream("highlow_merge", highlow.range, rvConsolidator.data1).setInline(true);
      dag.addStream("volume_merge", minuteVolume.sum, rvConsolidator.data2).setInline(true);
      dag.addStream("minute_console", rvConsolidator.out, minuteConsole.input).setInline(true);

      dag.addStream("priceAverage_priceSma", priceAvg.average, priceSMA.data).setInline(true);
      dag.addStream("priceSma_smaCconsolidator", priceSMA.doubleSMA, smaConsolidator.data1).setInline(true);

      dag.addStream("volumeAverage_volumeSma", volumeAvg.average, volumeSMA.data).setInline(true);
      dag.addStream("volumeSma_smaCconsolidator", volumeSMA.longSMA, smaConsolidator.data2).setInline(true);
      dag.addStream("smaConsolidator_console", smaConsolidator.out, smaConsole.input).setInline(true);
    }
    else {
      // nothing
    }

    return dag;
  }
}