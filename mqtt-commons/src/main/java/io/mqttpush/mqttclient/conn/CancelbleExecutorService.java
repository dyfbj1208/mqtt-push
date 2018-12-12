package io.mqttpush.mqttclient.conn;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 取消执行的线程池
 * @author tianzhenjiu
 *
 */
public class CancelbleExecutorService extends ScheduledThreadPoolExecutor{

	
	BlockingQueue<Future<?>> futures=new LinkedBlockingQueue<>(1024);
	public CancelbleExecutorService(int corePoolSize) {
		super(corePoolSize);
		
	}

	
	/**
	 * 删除所有已有的任务
	 */
	public void reset() {
		BlockingQueue<Runnable> blockingQueue=super.getQueue();
		if(blockingQueue!=null) {
			blockingQueue.forEach((b)->{
				CancelbleExecutorService.this.remove(b);
			});
		}
		
	}

	
}
