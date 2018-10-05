package io.netty.example.redis;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import io.netty.util.internal.PlatformDependent;

public class Locks {

	
	public void test(Object id) throws InterruptedException {
		
		synchronized (id) {
			System.out.println(Thread.currentThread()+"start-test");
			TimeUnit.SECONDS.sleep(1);
			System.out.println(Thread.currentThread()+"end-test");
		}
	}
	
	public void tes2t(Object id) throws InterruptedException {
		
		
		synchronized (id) {			
			System.out.println(Thread.currentThread()+"start-tes2t");
			TimeUnit.SECONDS.sleep(3);
			System.out.println(Thread.currentThread()+"end-tes2t");
		}
		
	}
	public static void main(String[] args) throws InterruptedException {
		
		DefaultChannelGroup channelGroup=new DefaultChannelGroup("",new UnorderedThreadPoolEventExecutor(4));
		
		PlatformDependent.newConcurrentHashMap();
		
		final Locks locks=new Locks();
		
		new Thread() {@Override
		public void run() {
			try {
				locks.tes2t(11);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}}.start();;
		
		
		new Thread() {@Override
			public void run() {
				try {
					locks.test(112);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}.start();;
		
	}
}
