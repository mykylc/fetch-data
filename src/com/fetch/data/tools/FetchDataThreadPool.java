package com.fetch.data.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FetchDataThreadPool {
	 private static final int QUEUE_NUMBER = 500;
	 public static final ExecutorService exec =  new ThreadPoolExecutor(350, 500, 0L, TimeUnit.MILLISECONDS,  
			 new LinkedBlockingQueue<Runnable>(QUEUE_NUMBER),
			 new ThreadFactory() {
	        private final String threadPoolPrefix = "FetchDataThreadPool-Thread-";
	        public  AtomicInteger counter = new AtomicInteger(0);
	        public Thread newThread(Runnable r) {
	            Thread t = new Thread(r);
	            t.setName(threadPoolPrefix+counter.incrementAndGet());
	            if (t.isDaemon()){
	                t.setDaemon(false);
	            }

	            if (t.getPriority() != Thread.NORM_PRIORITY){
	                t.setPriority(Thread.NORM_PRIORITY);
	            }
	            return t;
	        }
	 });
}
