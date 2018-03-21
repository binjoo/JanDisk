package disk;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class WeChatThreadFactory implements ThreadFactory {
    private AtomicInteger threadCounter = new AtomicInteger();
    private String name;

    public WeChatThreadFactory(String name) {
        this.name = name;
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(name + "-" + threadCounter.incrementAndGet());
        Thread.UncaughtExceptionHandler logHander = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                // logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        };
        thread.setUncaughtExceptionHandler(logHander);
        return thread;
    }
}
