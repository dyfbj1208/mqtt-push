package io.mqttpush.mqttserver.util.thread;

import org.apache.log4j.Logger;

/**
 * 
 *  CPU亲缘行的runnable对象
 *  凡是使用SignelThreadPoll  运行的runnable必须是MyHashRunnable
 *  MyHashRunnable 才能制定hash标识
 * @author acer
 *
 */
public class MyHashRunnable implements Runnable {

	Logger logger=Logger.getLogger(getClass());
	String identify;
	Runnable runnable;
	int retrys;

	public MyHashRunnable(String identify, Runnable runnable, int retrys) {
		super();
		this.identify = identify;
		this.runnable = runnable;
		this.retrys = retrys;
	}

	public void run() {
		try {
			this.runnable.run();
		} catch (Exception e) {
			logger.warn("出现了异常,但是已经捕获了",e);
		}
	}
}