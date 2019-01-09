package io.mqttpush.mqttserver.util.thread;

import java.util.concurrent.Future;
import java.util.function.BiConsumer;



/**
 * 
 *  CPU亲缘行的runnable对象
 * @author acer
 *
 */
public class MyHashRunnable implements Runnable {


	/**
	 * hash标志
	 */
	String identify;
	
	/**
	 * 实际的runnable对象
	 */
	Runnable runnable;
	
	/**
	 * 重试次数
	 */
	int retrys;
	
	/**
	 * 标志这个runnable对象的线程
	 */
	Thread thread;
	/**
	 * 标准这个runnable对象提交返回的future
	 */
	Future<?> future;
	
	Future<?> checkFuture;
	
	/**
	 * 任务开始执行的回调
	 */
	BiConsumer<MyHashRunnable,Long> whenStartCall;
	
	/**
	 * 任务完成时候的回调
	 */
	BiConsumer<MyHashRunnable, Throwable> whenFinishCall;
	
	Class<?> sourceClass;
	
	public MyHashRunnable(Class<?> sourceClass,String identify, Runnable runnable, int retrys) {
		super();
		this.sourceClass=sourceClass;
		this.identify = identify;
		this.runnable = runnable;
		this.retrys = retrys;
		
	}

	

	public void run() {
		

		this.thread=Thread.currentThread();
		Throwable runExcep=null;
		if(whenStartCall!=null) {
			whenStartCall.accept(this,System.currentTimeMillis());
		}
		
		try {
			this.runnable.run();
		} catch (Throwable e) {
			runExcep=e;
		}finally {
			if(!checkFuture.isCancelled()) {				
				checkFuture.cancel(true);
			}
			if(whenFinishCall!=null) {
				whenFinishCall.accept(this, runExcep);
			}
		}
		

	}
	
}