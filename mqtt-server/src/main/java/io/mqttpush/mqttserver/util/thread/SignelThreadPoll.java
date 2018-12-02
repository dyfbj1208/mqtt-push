package io.mqttpush.mqttserver.util.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
public class SignelThreadPoll {

	int threadCount;
	LoopSingelThread[] loopSingelThreads;
	BlockingQueue<Runnable> workQueue;;

	public SignelThreadPoll(int threadCount) {
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

	public void execute(Runnable command) {
		if (loopSingelThreads != null && loopSingelThreads.length > 0) {
			loopSingelThreads[0].execute(command);
		}
	}

}