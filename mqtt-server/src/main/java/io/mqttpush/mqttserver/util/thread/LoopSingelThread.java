package io.mqttpush.mqttserver.util.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;







/**
 * 
 * @author tianzhenjiu
 * 
 * 这是一个单线程的双向链表
 * 方便在判断这个线程不能处理runnable的时候 丢给下一个线程
 * 
 *  
 *
 */
public  class LoopSingelThread extends ThreadPoolExecutor {

	LoopSingelThread next;

	LoopSingelThread prev;

	
	public static Logger logger=Logger.getLogger(LoopSingelThread.class);

	int index;

	int totalThreadCount;

	public LoopSingelThread(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
		super(1, 1, 0, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(10240),new MyThreadFactory());

	}

	@Override
	public void execute(Runnable command) {

		if (command instanceof MyHashRunnable) {

			MyHashRunnable me = (MyHashRunnable) command;
			if (canExe(me)) {
				logger.info(super.hashCode()+"直接处理"+index+"->"+me.identify);
				super.execute(command);
			} else if(next!=null){
				logger.info(index+"无法处理,接下一个处理");
				next.execute(command);
			}else {
				logger.info("寻找匹配线程失败"+me.identify);
				super.execute(command);
			}

		} else {
			logger.info("非HashRun直接处理");
			super.execute(command);
		}
	}

	public boolean canExe(MyHashRunnable hashRunnable) {

		String identify = hashRunnable.identify;
		int hash=Math.abs( identify.hashCode());
		return hash % totalThreadCount == index;
	}
	
	
	/**
	 * 自己的线程工厂，方便设置线程的属性
	 * @author acer
	 *
	 */
	public static class MyThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(0);
		private final String namePrefix;

		MyThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "service-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			int num = threadNumber.getAndIncrement();
			Thread t = new Thread( group, r, namePrefix + num, 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);

			t.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
			return t;
		}

	}

	public int getTotalThreadCount() {
		return totalThreadCount;
	}

	public void setTotalThreadCount(int totalThreadCount) {
		this.totalThreadCount = totalThreadCount;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public LoopSingelThread getNext() {
		return next;
	}

	public void setNext(LoopSingelThread next) {
		this.next = next;
	}

	public LoopSingelThread getPrev() {
		return prev;
	}

	public void setPrev(LoopSingelThread prev) {
		this.prev = prev;
	}

}