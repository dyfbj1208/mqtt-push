package io.netty.example.map;

import java.security.KeyStore.Entry;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.netty.util.internal.PlatformDependent;

public class TestMap<K,V> extends HashMap<K, V> {

	
	public static class TestKey{
		
		String key;
		Long timestamp;
		
		
		public TestKey(String key, Long timestamp) {
			super();
			this.key = key;
			this.timestamp = timestamp;
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return key.hashCode();
		}
		
		
		
		
	}
	public static void main(String[] args) throws InterruptedException {
		
				
	
		final Map<String, String> concurrentMap=new Hashtable<>();
		
		ThreadPoolExecutor executorService= new ThreadPoolExecutor(10, 20,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(20000000));
		
		
		final ThreadLocal<MyRunnable> threadLocal=new ThreadLocal<>();
		final ThreadLocal<MyRunnableR> threadLocalR=new ThreadLocal<>();
//		
//		Runnable runnable=null;
//		
//		Runnable runnableR=new MyRunnableR(null, concurrentMap);
		
		final  int count=10000000;
		final CountDownLatch countDownLatch=new CountDownLatch(count<<1);
		Long l1=System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			
		
			final String k=""+System.currentTimeMillis();
			
			MyRunnable runnable=threadLocal.get();
			if(runnable==null) {
				threadLocal.set(runnable=new MyRunnable(k, concurrentMap,countDownLatch));
			}else {
				runnable.setKey(k);
			}
			
			executorService.submit(runnable);
			
			
	
			MyRunnableR runnabler=threadLocalR.get();
			if(runnabler==null) {
				threadLocalR.set(runnabler=new MyRunnableR(k, concurrentMap,countDownLatch));
			}else {
				runnabler.setKey(k);
			}
			
			executorService.submit(runnabler);
			
	//		concurrentMap.remove(k);
		
			
		}
		
		
		countDownLatch.await();
		executorService.shutdown();
		Long l2=System.currentTimeMillis();
		System.out.println(l2-l1);
	}
	
	
	public  static  class MyRunnable  implements Runnable{
		
		protected String key;
		
		protected  Map<String, String> concurrentMap;
		 
		CountDownLatch countDownLatch;

		



		public MyRunnable(String key, Map<String, String> concurrentMap, CountDownLatch countDownLatch) {
			super();
			this.key = key;
			this.concurrentMap = concurrentMap;
			this.countDownLatch = countDownLatch;
		}




		/**
		 * @return the key
		 */
		public String getKey() {
			return key;
		}




		/**
		 * @return the concurrentMap
		 */
		public Map<String, String> getConcurrentMap() {
			return concurrentMap;
		}




		/**
		 * @param key the key to set
		 */
		public void setKey(String key) {
			this.key = key;
		}




		/**
		 * @param concurrentMap the concurrentMap to set
		 */
		public void setConcurrentMap(Map<String, String> concurrentMap) {
			this.concurrentMap = concurrentMap;
		}




		@Override
		public void run() {
			concurrentMap.put(key, key);
			countDownLatch.countDown();
		}
		
	}
	
	public  static  class MyRunnableR  extends MyRunnable{
		


		

		public MyRunnableR(String key, Map<String, String> concurrentMap, CountDownLatch countDownLatch) {
			super(key, concurrentMap, countDownLatch);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			countDownLatch.countDown();
			concurrentMap.remove(key);
		}
		
	}
}
