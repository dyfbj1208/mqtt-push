package io.mqttpush.mqttserver.util.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * 
 * CPU亲缘行的线程池
 * 
 * @author acer
 *
 */
public class SingelThreadPool {

	public Logger logger = Logger.getLogger(SingelThreadPool.class);

	/**
	 * 定时检测超时任务
	 */
	ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
	/**
	 * 超时时间 默认16秒
	 */
	final long taskTimeOut = 16000;
	int threadCount;
	LoopSingelThread[] loopSingelThreads;

	static SingelThreadPool signelThreadPoll;

	public static SingelThreadPool getinstance() {

		if (signelThreadPoll == null) {
			signelThreadPoll = new SingelThreadPool(32);
		}
		return signelThreadPoll;
	}

	private SingelThreadPool(int threadCount) {
		super();
		this.threadCount = threadCount;
		loopSingelThreads = new LoopSingelThread[threadCount];

		LoopSingelThread prev = new LoopSingelThread(1, 1, 0, TimeUnit.MICROSECONDS);
		prev.setTotalThreadCount(threadCount);
		prev.setIndex(0);
		loopSingelThreads[0] = prev;

		for (int i = 1; i < threadCount; i++) {
			LoopSingelThread curt = new LoopSingelThread(1, 1, 0, TimeUnit.MICROSECONDS);
			loopSingelThreads[i] = curt;
			curt.setTotalThreadCount(threadCount);
			curt.setIndex(i);

			curt.setPrev(prev);
			prev.setNext(curt);
			prev = curt;

		}
	}

	
	
	/**
	 * 当线程开始执行时候的回调
	 * 如有异常需要自行处理
	 * @param taskRun
	 * @param taskStartTime
	 */
	void  whenTaskStart(MyHashRunnable taskRun, Long taskStartTime) {
		
		try {
			/**
			 * 超时时间检测任务是否超时
			 */
			 ScheduledFuture<?> future=executorService.schedule(() -> {
				checkTaskQueue( taskRun, taskStartTime);
			}, taskTimeOut, TimeUnit.MILLISECONDS);
			 
			 taskRun.checkFuture=future;
			 
			 if(logger.isInfoEnabled()) {
				 logger.info("task->"+taskRun.identify+"开始执行:"+taskStartTime.longValue());
			 }
			
		} catch (Exception e) {
			 logger.warn("task->"+taskRun.identify+"开始回调异常",e);
		}
		
	}
	
	
	/**
	 * 当有任务完成执行时候的回调
	 * @param taskRun 
	 * @param e  任务可能有异常
	 */
	void  whenTaskFinish(MyHashRunnable taskRun,Throwable e) {
		
		String threadid="task->"+taskRun.identify;
		if(e!=null) {
			 logger.warn(threadid+"执行任务异常",e);
		}else {
			 if(logger.isInfoEnabled()) {
				 logger.info(threadid+"完成:");
			 }
		}
	}

	/**
	 * 检测任务是否超时
	 * 
	 * @param poolExecutor
	 * @param taskRun
	 */
	 void checkTaskQueue( MyHashRunnable taskRun, Long taskStartTime) {

		/**
		 * 检测runnable是否超时
		 * 如果
		 */
		if(taskRun.future==null) {
			logger.warn("无法检查没有future的任务->"+taskRun.identify);
			return;
		}
		
		if (taskRun instanceof MyHashRunnable) {
			long now = System.currentTimeMillis();

			if (now - taskStartTime >= taskTimeOut) {				
				boolean iscanceled =taskRun.future.cancel(true);
				logger.info(taskRun.sourceClass+":异常超时任务->" + taskRun.identify + "取消执行" + iscanceled);
			} else {
				logger.debug("已经完成");
			}
		} else {
			logger.warn("无法检查非MyHashRunnable的任务");
		}

	}

	/**
	 * 提交任务
	 * @param command
	 * @return
	 */
	public Future<?> submit(Runnable command) {

		if (command instanceof MyHashRunnable) {
			MyHashRunnable me = (MyHashRunnable) command;
			me.whenStartCall = this::whenTaskStart;
			me.whenFinishCall=this::whenTaskFinish;
		}

		Future<?> future = null;
		if (loopSingelThreads != null && loopSingelThreads.length > 0) {
			future = loopSingelThreads[0].submit(command);
		}

		return future;
	}

	/**
	 * 执行没有future的runnable对象
	 * 
	 * @param command
	 */
	public void executeWithoutFuture(Runnable command) {

		if (command instanceof MyHashRunnable) {
			MyHashRunnable me = (MyHashRunnable) command;
			me.whenStartCall = this::whenTaskStart;
			me.whenFinishCall=this::whenTaskFinish;
		}

		if (loopSingelThreads != null && loopSingelThreads.length > 0) {
			loopSingelThreads[0].execute(command);
		}
	}

	/**
	 * 执行有future的任务
	 * @param command
	 */
	public void execute(Runnable command) {
			
		Future<?> future=submit(command);
		if (command instanceof MyHashRunnable) {
			MyHashRunnable me = (MyHashRunnable) command;
			me.future=future;
		}
	}

}