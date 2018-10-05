package io.netty.example.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class Locks2    {

	volatile Map<String, String> map=new HashMap(1000);
	
	
	static  int  devintnum=0;
	static AtomicInteger atomicInteger=new AtomicInteger(0);
	
	
	static volatile int tempnum=0;	
	
	public  void  login(String devid,String channel) {
		String idchannel=map.put(devid, channel);
		atomicInteger.incrementAndGet();
		tempnum++;
		if(idchannel!=null) {
			System.out.println(atomicInteger.get()+"\t"+channel);
		}
	}

	public static void main(String[] args) throws InterruptedException {
//		
//	
//		final Locks2 locks2=new Locks2();
//		
//		
//		Thread[] threads=new Thread[10000];
//		for (int i = 0; i <threads.length; i++) {
//			
//			threads[i]=new Thread() {
//				
//				public void  run() {
//					locks2.login(String.valueOf(++devintnum),String.valueOf(devintnum) );
//				}
//				
//			};
//			threads[i].start();
//		}
//		
//		
//		for (int i = 0; i < threads.length; i++) {
//			threads[i].join();
//		}
//		
//		System.out.println(atomicInteger.get());
//		System.out.println(devintnum);

	}

}
