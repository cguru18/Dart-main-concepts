package com.studentmanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized thread pool management for the application
 */
public class ThreadPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);
    
    private static ThreadPoolManager instance;
    
    // Different thread pools for different types of operations
    private final ExecutorService databaseExecutor;
    private final ExecutorService uiExecutor;
    private final ExecutorService reportExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    
    // Thread pool configurations
    private static final int DATABASE_POOL_SIZE = 10;
    private static final int UI_POOL_SIZE = 5;
    private static final int REPORT_POOL_SIZE = 3;
    private static final int SCHEDULED_POOL_SIZE = 2;
    
    private volatile boolean isShutdown = false;

    private ThreadPoolManager() {
        // Database operations thread pool
        this.databaseExecutor = Executors.newFixedThreadPool(
            DATABASE_POOL_SIZE, 
            new CustomThreadFactory("Database-Worker")
        );
        
        // UI operations thread pool
        this.uiExecutor = Executors.newFixedThreadPool(
            UI_POOL_SIZE, 
            new CustomThreadFactory("UI-Worker")
        );
        
        // Report generation thread pool
        this.reportExecutor = Executors.newFixedThreadPool(
            REPORT_POOL_SIZE, 
            new CustomThreadFactory("Report-Worker")
        );
        
        // Scheduled tasks thread pool
        this.scheduledExecutor = Executors.newScheduledThreadPool(
            SCHEDULED_POOL_SIZE, 
            new CustomThreadFactory("Scheduled-Worker")
        );
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        
        logger.info("Thread pool manager initialized with {} database, {} UI, {} report, and {} scheduled threads",
                   DATABASE_POOL_SIZE, UI_POOL_SIZE, REPORT_POOL_SIZE, SCHEDULED_POOL_SIZE);
    }

    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    /**
     * Submit database operation
     */
    public <T> CompletableFuture<T> submitDatabaseTask(Callable<T> task) {
        if (isShutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Thread pool is shutdown"));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                logger.error("Database task execution failed", e);
                throw new RuntimeException(e);
            }
        }, databaseExecutor);
    }

    /**
     * Submit UI operation
     */
    public <T> CompletableFuture<T> submitUITask(Callable<T> task) {
        if (isShutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Thread pool is shutdown"));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                logger.error("UI task execution failed", e);
                throw new RuntimeException(e);
            }
        }, uiExecutor);
    }

    /**
     * Submit report generation task
     */
    public <T> CompletableFuture<T> submitReportTask(Callable<T> task) {
        if (isShutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Thread pool is shutdown"));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                logger.error("Report task execution failed", e);
                throw new RuntimeException(e);
            }
        }, reportExecutor);
    }

    /**
     * Schedule recurring task
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, 
                                                  long period, TimeUnit unit) {
        if (isShutdown) {
            throw new IllegalStateException("Thread pool is shutdown");
        }
        
        return scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                command.run();
            } catch (Exception e) {
                logger.error("Scheduled task execution failed", e);
            }
        }, initialDelay, period, unit);
    }

    /**
     * Schedule one-time delayed task
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (isShutdown) {
            throw new IllegalStateException("Thread pool is shutdown");
        }
        
        return scheduledExecutor.schedule(() -> {
            try {
                command.run();
            } catch (Exception e) {
                logger.error("Scheduled task execution failed", e);
            }
        }, delay, unit);
    }

    /**
     * Execute multiple tasks in parallel and wait for all to complete
     */
    public <T> CompletableFuture<List<T>> executeInParallel(List<Callable<T>> tasks) {
        if (isShutdown) {
            return CompletableFuture.failedFuture(new IllegalStateException("Thread pool is shutdown"));
        }
        
        List<CompletableFuture<T>> futures = tasks.stream()
            .map(task -> submitDatabaseTask(task))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    /**
     * Get thread pool statistics
     */
    public String getThreadPoolStats() {
        StringBuilder stats = new StringBuilder();
        
        if (databaseExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor dbPool = (ThreadPoolExecutor) databaseExecutor;
            stats.append(String.format("Database Pool - Active: %d, Queue: %d, Completed: %d\n",
                dbPool.getActiveCount(), dbPool.getQueue().size(), dbPool.getCompletedTaskCount()));
        }
        
        if (uiExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor uiPool = (ThreadPoolExecutor) uiExecutor;
            stats.append(String.format("UI Pool - Active: %d, Queue: %d, Completed: %d\n",
                uiPool.getActiveCount(), uiPool.getQueue().size(), uiPool.getCompletedTaskCount()));
        }
        
        if (reportExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor reportPool = (ThreadPoolExecutor) reportExecutor;
            stats.append(String.format("Report Pool - Active: %d, Queue: %d, Completed: %d\n",
                reportPool.getActiveCount(), reportPool.getQueue().size(), reportPool.getCompletedTaskCount()));
        }
        
        return stats.toString();
    }

    /**
     * Graceful shutdown of all thread pools
     */
    public void shutdown() {
        if (isShutdown) {
            return;
        }
        
        isShutdown = true;
        logger.info("Shutting down thread pools...");
        
        // Shutdown all executors
        shutdownExecutor(databaseExecutor, "Database");
        shutdownExecutor(uiExecutor, "UI");
        shutdownExecutor(reportExecutor, "Report");
        shutdownExecutor(scheduledExecutor, "Scheduled");
        
        logger.info("Thread pool manager shutdown completed");
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("{} executor did not terminate gracefully, forcing shutdown", name);
                executor.shutdownNow();
                
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.error("{} executor did not terminate after forced shutdown", name);
                }
            } else {
                logger.debug("{} executor shutdown successfully", name);
            }
        } catch (InterruptedException e) {
            logger.warn("{} executor shutdown interrupted", name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Custom thread factory for better thread naming and monitoring
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = "SMS-" + namePrefix + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            thread.setDaemon(true); // Don't prevent JVM shutdown
            thread.setPriority(Thread.NORM_PRIORITY);
            
            // Set uncaught exception handler
            thread.setUncaughtExceptionHandler((t, e) -> 
                logger.error("Uncaught exception in thread {}", t.getName(), e));
            
            return thread;
        }
    }

    /**
     * Monitor thread pool health
     */
    public void startHealthMonitoring() {
        scheduleAtFixedRate(() -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Thread Pool Health Check:\n{}", getThreadPoolStats());
            }
        }, 5, 5, TimeUnit.MINUTES);
    }
}