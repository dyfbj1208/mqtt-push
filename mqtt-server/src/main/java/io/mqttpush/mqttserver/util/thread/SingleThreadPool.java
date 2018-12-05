package io.mqttpush.mqttserver.util.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *  * 
 * hash标识跟随线程的 线程池
 *  使用这样的线程可以保证 相同的标识被相同的线程处理
 *  
 * CPU亲缘性的
 * @author acer
 *
 */
public class SingleThreadPool {

	int threadCount;
	LoopSingleThread[] loopSingleThreads;
	BlockingQueue<Runnable> workQueue;;

	public SingleThreadPool(int threadCount) {
		super();
		this.threadCount = threadCount;
		loopSingleThreads = new LoopSingleThread[threadCount];
		

		LoopSingleThread prev = new LoopSingleThread(1, 1, 0, TimeUnit.MICROSECONDS);
		prev.setTotalThreadCount(threadCount);
		prev.setIndex(0);
		loopSingleThreads[0] = prev;

		for (int i = 1; i < threadCount; i++) {
			LoopSingleThread curt = new LoopSingleThread(1, 1, 0, TimeUnit.MICROSECONDS);
			loopSingleThreads[i] = curt;
			curt.setTotalThreadCount(threadCount);
			curt.setIndex(i);

			curt.setPrev(prev);
			prev.setNext(curt);
			prev = curt;

		}
	}

	public void execute(Runnable command) {
		if (loopSingleThreads != null && loopSingleThreads.length > 0) {
			loopSingleThreads[0].execute(command);
		}
	}

}