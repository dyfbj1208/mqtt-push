package io.netty.example.map;

import io.mqttpush.mqttserver.util.thread.MyHashRunnable;
import io.mqttpush.mqttserver.util.thread.SingleThreadPool;

public class TestThread {

	public static void main(String[] args) {

		final SingleThreadPool pool=new SingleThreadPool(4);
		for (int i = 0; i < 100; i++) {
			pool.execute(new MyHashRunnable(""+i, new ParameterRunnable(i), 0));
		}


	}

	static class ParameterRunnable implements Runnable {

		int num;

		public ParameterRunnable(int num) {
			this.num = num;
		}

		@Override
		public void run() {

			System.out.println(Thread.currentThread() + "--------->" + num);
			if (num == 5) {
				System.out.print(100 / 0);
			}
		}
	}

}
