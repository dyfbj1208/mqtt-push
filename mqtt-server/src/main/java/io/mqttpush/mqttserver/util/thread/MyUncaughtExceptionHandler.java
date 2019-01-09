package io.mqttpush.mqttserver.util.thread;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

/**
 * 
 * @author tianzhenjiu
 *
 */
public class MyUncaughtExceptionHandler  implements UncaughtExceptionHandler{

	Logger logger=Logger.getLogger(getClass());
	
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		
		logger.warn("发送了异常,非常严重",e);
	}

}
