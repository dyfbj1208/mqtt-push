package io.mqttpush.mqttserver.util.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
/**
 * 当拒绝任务添加的时候
 * 
 * 这里的处理策略是当拒绝添加的时候 就在当前线程取处理把
 * @author tianzhenjiu
 *
 */
public class MyRejectedExecutionHandler implements RejectedExecutionHandler{

	
	public static Logger logger = Logger.getLogger(MyRejectedExecutionHandler.class);
	
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		
	
		try {
			r.run();
		} catch (Exception e) {
			logger.warn("直接执行runnable失败",e);
		}
		
		if(logger.isInfoEnabled()) {
			logger.info("队列是否已经满了?,已经在当前线程执行"+executor.getQueue().size());
		}
		
	}

	
}
