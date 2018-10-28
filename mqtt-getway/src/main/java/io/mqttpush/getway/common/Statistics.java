package io.mqttpush.getway.common;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;


public class Statistics implements Runnable{


    Logger logger=Logger.getLogger(getClass());

    /**
     * http前端连接数
     */
    public static AtomicInteger httpAbCount=new AtomicInteger(0);

    /**
     * 回调http后端连接数
     */
    public  static  AtomicInteger httpBcCount=new AtomicInteger(0);

    /**
     * http 前端请求接口数
     */
    public static AtomicInteger httpReqCount=new AtomicInteger(0);

    /**
     * http后端响应回调接口数
     */
    public static AtomicInteger httpResCount=new AtomicInteger(0);
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

    private static Statistics statistics=new Statistics();

     private  Statistics(){

     }

     public static  Statistics instance(){
         return statistics;
     }
    @Override
    public void run() {

        if(logger.isInfoEnabled()){

            int i1=requestCount.getAndSet(0);
            int i2=responseCount.getAndSet(0);

            int abCon=aBconnCount.get();
            int bCCon=bCconnCount.get();

            if(logger.isInfoEnabled()){

                logger.info("http前端连接数"+httpAbCount.get());
                logger.info("http后端连接数"+httpBcCount.get());
                logger.info("http前端请求数"+httpReqCount.getAndSet(0));
                logger.info("http后端回调数"+httpResCount.getAndSet(0));


                logger.info("请求总是"+i1);
                logger.info("响应总是"+i2);

                logger.info("前端连接总数"+abCon);
                logger.info("后端连接总数"+bCCon);
            }

            long nowTimeStamp=System.currentTimeMillis();
            long  diffSeconds=(nowTimeStamp-lastTimeStamp)/1000;
            logger.info("QPS="+(i2*1000)/diffSeconds);
            lastTimeStamp=nowTimeStamp;
        }
    }
}
