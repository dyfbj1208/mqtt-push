package io.mqttpush.getway.common;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;


public class Statistics implements Runnable{


    Logger logger=Logger.getLogger(getClass());

    /**
     * 前端连接数
     */
    public static AtomicInteger aBconnCount=new AtomicInteger(0);

    /**
     * 后端连接数
     */
    public static AtomicInteger bCconnCount=new AtomicInteger(0);
    /**
     * 请求数量
     */
    public static AtomicInteger requestCount=new AtomicInteger(0);

    /**
     * 响应数
     */
    public static AtomicInteger  responseCount=new AtomicInteger(0);

     long lastTimeStamp=0;

    @Override
    public void run() {

        if(logger.isInfoEnabled()){

            int i1=requestCount.get();
            int i2=responseCount.get();

            int abCon=aBconnCount.get();
            int bCCon=bCconnCount.get();

            logger.info("请求总是"+i1);
            logger.info("响应总是"+i2);

            logger.info("前端连接总数"+abCon);
            logger.info("后端连接总数"+bCCon);

            long nowTimeStamp=System.currentTimeMillis();
            long  diffSeconds=(nowTimeStamp-lastTimeStamp)/1000;
            logger.info("QPS="+(i2*1000)/diffSeconds);
            lastTimeStamp=nowTimeStamp;
        }
    }
}
