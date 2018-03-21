package disk;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private ScheduledExecutorService scheduledThreadPool;
    private ThreadPoolExecutor workerThreadPool;
    private LinkedBlockingQueue<Runnable> requestQueue;
    private int coreWorkerPoolSize = 16; // 核心线程池大小
    private int maxWorkerPoolSize = 32; // 最大线程池大小
    private int scheduledPoolSize = 5;

    public ThreadPool() {
    }

    public void start() {
        int linkedBlockingQueueSize = coreWorkerPoolSize * 2;
        requestQueue = new LinkedBlockingQueue<Runnable>(linkedBlockingQueueSize);

        scheduledThreadPool = new ScheduledThreadPoolExecutor(
                scheduledPoolSize,
                new WeChatThreadFactory("WeChatSchedule"), 
                new ThreadPoolExecutor.AbortPolicy());

        workerThreadPool = new ThreadPoolExecutor(
                coreWorkerPoolSize, 
                maxWorkerPoolSize, 
                60L, 
                TimeUnit.SECONDS,
                requestQueue, 
                new WeChatThreadFactory("WeChatWorker"));

        workerThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void executeThreadWorker(Runnable worker) {
        workerThreadPool.execute(worker);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledThreadPool.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
}
