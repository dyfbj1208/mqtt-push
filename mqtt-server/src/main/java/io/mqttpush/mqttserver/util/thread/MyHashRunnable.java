package io.mqttpush.mqttserver.util.thread;

/**
 * 
 *  CPU亲缘行的runnable对象
 *  凡是使用SignelThreadPoll  运行的runnable必须是MyHashRunnable
 *  MyHashRunnable 才能制定hash标识
 * @author acer
 *
 */
public class MyHashRunnable implements Runnable {

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
		this.runnable.run();
	}
}