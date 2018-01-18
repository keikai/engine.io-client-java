/* EventThreadHelper.java

	Purpose:
		
	Description:
		
	History:
		3:33 PM 15/01/2018, Created by jumperchen

Copyright (C) 2018 Potix Corporation. All Rights Reserved.
*/
package kk.socket.thread;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jumperchen
 */
public class EventThreadHelper {
	private static final Logger logger = Logger.getLogger(EventThreadHelper.class.getName());

	public static class NamedThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final ThreadGroup group;

		public NamedThreadFactory(String poolPrefix, String threadPrefix) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
					Thread.currentThread().getThreadGroup();
			namePrefix = poolPrefix + "-" +
					poolNumber.getAndIncrement() +
					"-" + threadPrefix + "-";

		}
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
					namePrefix + threadNumber.getAndIncrement(),
					0);
			t.setDaemon(Thread.currentThread().isDaemon());
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	public static class ExecutorServiceHelper implements ExecutorService {
		public Thread thread;
		ThreadPoolExecutor _service;
		ExecutorServiceHelper(ThreadPoolExecutor service) {
			_service = service;
			_service.setThreadFactory(THREAD_FACTORY);
		}

		public ThreadPoolExecutor getExecutor() {
			return _service;
		}

		final ThreadFactory THREAD_FACTORY = new NamedThreadFactory("socketPool", "eventThread") {
		@Override
		public Thread newThread(Runnable runnable) {
			thread = super.newThread(runnable);
			return thread;
		}
	};

		public void shutdown() {
			_service.shutdown();
		}

		public List<Runnable> shutdownNow() {
			return _service.shutdownNow();
		}

		public boolean isShutdown() {
			return _service.isShutdown();
		}

		public boolean isTerminated() {
			return _service.isTerminated();
		}

		public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
			return _service.awaitTermination(timeout, unit);
		}

		public <T> Future<T> submit(Callable<T> task) {
			return _service.submit(task);
		}

		public <T> Future<T> submit(Runnable task, T result) {
			return _service.submit(task, result);
		}

		public Future<?> submit(Runnable task) {
			return _service.submit(task);
		}

		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> tasks)
				throws InterruptedException {
			return _service.invokeAll(tasks);
		}

		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> tasks, long timeout,
				TimeUnit unit) throws InterruptedException {
			return _service.invokeAll(tasks, timeout, unit);
		}

		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			return _service.invokeAny(tasks);
		}

		public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
				long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			return _service.invokeAny(tasks, timeout, unit);
		}

		public void execute(Runnable command) {
			_service.execute(command);
		}
	}
	public static ExecutorServiceHelper newFixedThreadPool(int nThreads) {
		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(nThreads);
		return new ExecutorServiceHelper(executorService);
	}

	public static void exec(Runnable task, ExecutorService service) {
		if (service.isShutdown() || service.isTerminated()) {
			task.run(); // run directly
		}

		if (((ExecutorServiceHelper) service).thread == Thread.currentThread()) {
			task.run();
		} else {
			nextTick(task, service);
		}
	}

	public static void nextTick(Runnable task, ExecutorService service) {
		if (service.isShutdown() || service.isTerminated()) {
			CompletableFuture.runAsync(task);
		} else {
			CompletableFuture.runAsync(() -> task.run(), service).exceptionally((t) -> {
				logger.log(Level.SEVERE, "task threw exception", t);
				return null;
			});
		}
	}
}
