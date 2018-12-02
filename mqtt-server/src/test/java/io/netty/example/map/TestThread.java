package io.netty.example.map;

import io.mqttpush.mqttserver.util.thread.MyHashRunnable;
import io.mqttpush.mqttserver.util.thread.SignelThreadPoll;

public class TestThread {

	public static void main(String[] args) {
		
		
		final SignelThreadPoll poll=new SignelThreadPoll(4);
		for (int i = 0; i < 1000; i++) {
			new  Thread() {

				@Override
				public void run() {
					poll.execute(new MyHashRunnable("clifenta", new Runnable() {
						
						@Override
						public void run() {
						
							System.out.println(Thread.currentThread());
							
						}
					}, 0));
				}
				
				
				
			}.start();
		}
		
		
		
		
		

	}

}
