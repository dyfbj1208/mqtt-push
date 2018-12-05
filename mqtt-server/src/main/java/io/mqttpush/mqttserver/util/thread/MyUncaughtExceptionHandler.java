package io.mqttpush.mqttserver.util.thread;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;


/**
 * 
 * @author tzj
 *
 */
public class MyUncaughtExceptionHandler  implements UncaughtExceptionHandler{

	public static Logger logger=Logger.getLogger(MyUncaughtExceptionHandler.class);
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		
		logger.warn(t.getName()+"异常,将会结束掉!!!",e);
	}

}
