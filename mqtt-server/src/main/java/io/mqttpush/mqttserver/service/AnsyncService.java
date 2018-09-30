package io.mqttpush.mqttserver.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * 执行用户线程的任务
 * 
 * @author acer
 *
 */
public class AnsyncService {

	Logger logger=Logger.getLogger(getClass());
	final ExecutorService userThread = Executors.newWorkStealingPool();

	final ExecutorService saveMsgRepThread = Executors.newSingleThreadExecutor();

	//ConcurrentHashMap<Channel, Map<Integer, MessageFuture>> concurrentHashMap = new ConcurrentHashMap<>();

	public java.util.concurrent.Future runTask(Runnable runnable) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error(t+"异常",e);
			}
		});
		return userThread.submit(runnable);
	}

//	/**
//	 * 放一个future
//	 * 
//	 * @param channel
//	 * @param future
//	 * @return
//	 */
//	public void pushFuture(Channel channel, MessageFuture messageFuture) {
//
//		Map<Integer, MessageFuture> futures = null;
//		if (concurrentHashMap.containsKey(channel)) {
//			futures = concurrentHashMap.get(channel);
//		} else {
//			futures = new HashMap<>();
//			concurrentHashMap.put(channel, futures);
//		}
//
//		if (futures != null)
//			futures.put(messageFuture.getMessageId(), messageFuture);
//	}
//
//	public void removeFuture(Channel channel, Integer messageid) {
//
//		Map<Integer, MessageFuture> futures = null;
//		if (concurrentHashMap.containsKey(channel)) {
//			futures = concurrentHashMap.get(channel);
//		} else {
//			futures = new HashMap<>();
//			concurrentHashMap.put(channel, futures);
//		}
//
//		if (futures != null)
//			futures.remove(messageid);
//	}
//
//	public static class MessageFuture {
//
//		Future future;
//
//		Integer messageId;
//
//		public Future getFuture() {
//			return future;
//		}
//
//		public void setFuture(Future future) {
//			this.future = future;
//		}
//
//		public Integer getMessageId() {
//			return messageId;
//		}
//
//		public void setMessageId(Integer messageId) {
//			this.messageId = messageId;
//		}
//
//	}

}
