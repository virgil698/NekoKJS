package org.virgil.nekokjs.util;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * 异步任务管理器
 * 用于处理高频 Mixin 调用的异步执行
 * 
 * 特性：
 * - 线程池管理
 * - 任务队列
 * - 超时控制
 * - 性能监控
 */
public class AsyncTaskManager {
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Async");
    
    // 线程池配置
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 1000;
    
    // 超时配置
    private static final long DEFAULT_TIMEOUT_MS = 50; // 50ms 超时
    
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    
    // 性能统计
    private long totalTasks = 0;
    private long completedTasks = 0;
    private long timeoutTasks = 0;
    private long failedTasks = 0;
    
    public AsyncTaskManager() {
        // 创建线程池
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "NekoKJS-Async-" + counter++);
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时在调用线程执行
        );
        
        this.executorService = threadPool;
        this.scheduledExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, "NekoKJS-Scheduler");
            thread.setDaemon(true);
            return thread;
        });
        
        // 启动性能监控
        startPerformanceMonitor();
    }
    
    /**
     * 提交异步任务（带超时）
     * 
     * @param task 任务
     * @param timeoutMs 超时时间（毫秒）
     * @return Future 对象
     */
    public <T> Future<T> submitWithTimeout(Callable<T> task, long timeoutMs) {
        totalTasks++;
        
        CompletableFuture<T> future = new CompletableFuture<>();
        
        // 提交任务
        executorService.submit(() -> {
            try {
                T result = task.call();
                future.complete(result);
                completedTasks++;
            } catch (Exception e) {
                future.completeExceptionally(e);
                failedTasks++;
            }
        });
        
        // 设置超时
        scheduledExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Task timeout after " + timeoutMs + "ms"));
                timeoutTasks++;
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        
        return future;
    }
    
    /**
     * 提交异步任务（默认超时）
     */
    public <T> Future<T> submit(Callable<T> task) {
        return submitWithTimeout(task, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * 提交异步任务（无返回值）
     */
    public void execute(Runnable task) {
        totalTasks++;
        executorService.execute(() -> {
            try {
                task.run();
                completedTasks++;
            } catch (Exception e) {
                LOGGER.warning("Async task failed: " + e.getMessage());
                failedTasks++;
            }
        });
    }
    
    /**
     * 尝试获取结果（带超时）
     * 如果超时或失败，返回默认值
     */
    public <T> T getOrDefault(Future<T> future, T defaultValue, long timeoutMs) {
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            timeoutTasks++;
            return defaultValue;
        } catch (Exception e) {
            failedTasks++;
            return defaultValue;
        }
    }
    
    /**
     * 尝试获取结果（默认超时）
     */
    public <T> T getOrDefault(Future<T> future, T defaultValue) {
        return getOrDefault(future, defaultValue, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * 启动性能监控
     */
    private void startPerformanceMonitor() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (totalTasks > 0 && totalTasks % 10000 == 0) {
                double successRate = (double) completedTasks / totalTasks * 100;
                double timeoutRate = (double) timeoutTasks / totalTasks * 100;
                double failureRate = (double) failedTasks / totalTasks * 100;
                
                LOGGER.info(String.format(
                    "[AsyncTaskManager] Total: %d, Success: %.1f%%, Timeout: %.1f%%, Failed: %.1f%%",
                    totalTasks, successRate, timeoutRate, failureRate
                ));
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * 获取性能统计
     */
    public PerformanceStats getStats() {
        return new PerformanceStats(totalTasks, completedTasks, timeoutTasks, failedTasks);
    }
    
    /**
     * 关闭任务管理器
     */
    public void shutdown() {
        LOGGER.info("Shutting down AsyncTaskManager...");
        executorService.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        LOGGER.info("AsyncTaskManager shutdown complete");
    }
    
    /**
     * 性能统计数据
     */
    public static class PerformanceStats {
        public final long totalTasks;
        public final long completedTasks;
        public final long timeoutTasks;
        public final long failedTasks;
        
        public PerformanceStats(long totalTasks, long completedTasks, long timeoutTasks, long failedTasks) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.timeoutTasks = timeoutTasks;
            this.failedTasks = failedTasks;
        }
        
        public double getSuccessRate() {
            return totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        }
        
        public double getTimeoutRate() {
            return totalTasks > 0 ? (double) timeoutTasks / totalTasks * 100 : 0;
        }
        
        public double getFailureRate() {
            return totalTasks > 0 ? (double) failedTasks / totalTasks * 100 : 0;
        }
    }
}
