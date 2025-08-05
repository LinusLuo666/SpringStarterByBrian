package com.brian.springstarter.examples;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class VirtualThreadPerformanceDemo {

    // 模拟真实场景：大量IO密集型任务（HTTP调用、数据库查询等）
    private static final int TASK_COUNT = 10_000; // 1万个并发任务
    private static final int IO_DELAY_MS = 100;   // 每个任务100ms IO等待

    // 模拟IO操作
    private static String simulateIOOperation(int taskId) {
        try {
            // 模拟HTTP调用或数据库查询延迟
            Thread.sleep(IO_DELAY_MS);
            return "Task-" + taskId + " completed by " + Thread.currentThread();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Task-" + taskId + " interrupted";
        }
    }

    // 方案1：传统线程池（固定大小）
    public static Result traditionalThreadPool() {
        System.out.println("🔄 传统线程池方案...");
        
        int poolSize = 200; // 传统线程池通常限制在几百个
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        
        Instant start = Instant.now();
        List<CompletableFuture<String>> futures = IntStream.range(0, TASK_COUNT)
                .mapToObj(i -> CompletableFuture
                        .supplyAsync(() -> simulateIOOperation(i), executor))
                .toList();

        // 等待所有任务完成
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        Instant end = Instant.now();
        executor.shutdown();
        
        return new Result("传统线程池 (200线程)", Duration.between(start, end), results.size(), poolSize);
    }

    // 方案2：Cached线程池（无界）
    public static Result cachedThreadPool() {
        System.out.println("🔄 Cached线程池方案...");
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        Instant start = Instant.now();
        List<CompletableFuture<String>> futures = IntStream.range(0, TASK_COUNT)
                .mapToObj(i -> CompletableFuture
                        .supplyAsync(() -> simulateIOOperation(i), executor))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        Instant end = Instant.now();
        executor.shutdown();
        
        return new Result("Cached线程池 (无界)", Duration.between(start, end), results.size(), -1);
    }

    // 方案3：虚拟线程（Project Loom）
    public static Result virtualThreads() {
        System.out.println("🚀 虚拟线程方案...");
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        Instant start = Instant.now();
        List<CompletableFuture<String>> futures = IntStream.range(0, TASK_COUNT)
                .mapToObj(i -> CompletableFuture
                        .supplyAsync(() -> simulateIOOperation(i), executor))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        Instant end = Instant.now();
        executor.shutdown();
        
        return new Result("虚拟线程", Duration.between(start, end), results.size(), -1);
    }

    // 内存使用监控
    public static class MemoryMonitor {
        private static long getUsedMemory() {
            Runtime runtime = Runtime.getRuntime();
            return runtime.totalMemory() - runtime.freeMemory();
        }

        public static void monitorMemoryUsage(String scenario, Runnable task) {
            System.gc(); // 建议垃圾回收
            long memoryBefore = getUsedMemory();
            
            Instant start = Instant.now();
            task.run();
            Instant end = Instant.now();
            
            long memoryAfter = getUsedMemory();
            long memoryUsed = memoryAfter - memoryBefore;
            
            System.out.printf("📊 %s 内存使用: %,d bytes (%.2f MB)\n", 
                            scenario, memoryUsed, memoryUsed / 1024.0 / 1024.0);
            System.out.printf("⏱️  耗时: %d ms\n", Duration.between(start, end).toMillis());
            System.out.println();
        }
    }

    // 测试结果类
    public record Result(String name, Duration duration, int completedTasks, int threadCount) {
        public double getTasksPerSecond() {
            return completedTasks / (duration.toMillis() / 1000.0);
        }

        @Override
        public String toString() {
            return String.format("%s: %d tasks in %d ms (%.1f tasks/sec) using %s threads", 
                               name, completedTasks, duration.toMillis(), getTasksPerSecond(),
                               threadCount > 0 ? String.valueOf(threadCount) : "virtual");
        }
    }

    // 综合性能测试
    public static void runPerformanceTest() {
        System.out.println("=== 🚀 虚拟线程 vs 传统线程池性能对比 ===\n");
        System.out.println("场景: " + TASK_COUNT + " 个IO密集型任务，每个" + IO_DELAY_MS + "ms延迟\n");

        // 理论计算
        System.out.println("📈 理论性能分析:");
        System.out.println("   理论最小时间: " + IO_DELAY_MS + " ms (所有任务并行)");
        System.out.println("   传统线程池200线程: " + (TASK_COUNT * IO_DELAY_MS / 200) + " ms");
        System.out.println("   虚拟线程: " + IO_DELAY_MS + " ms (接近理论值)\n");

        // 实际测试
        List<Result> results = new ArrayList<>();

        // 小批量测试（演示用，减少等待时间）
        int demoTaskCount = 1_000; // 演示用1000个任务
        
        System.out.println("🔬 执行实际测试（" + demoTaskCount + "个任务）:\n");

        // 传统线程池
        MemoryMonitor.monitorMemoryUsage("传统线程池(50线程)", () -> {
            ExecutorService executor = Executors.newFixedThreadPool(50);
            List<CompletableFuture<Void>> futures = IntStream.range(0, demoTaskCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(IO_DELAY_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, executor))
                    .toList();
            futures.forEach(CompletableFuture::join);
            executor.shutdown();
        });

        // 虚拟线程
        MemoryMonitor.monitorMemoryUsage("虚拟线程", () -> {
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            List<CompletableFuture<Void>> futures = IntStream.range(0, demoTaskCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(IO_DELAY_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, executor))
                    .toList();
            futures.forEach(CompletableFuture::join);
            executor.shutdown();
        });

        // 总结
        System.out.println("=== 💡 性能总结 ===");
        System.out.println("虚拟线程优势:");
        System.out.println("  ✅ 内存效率: 1000个虚拟线程 ~1MB vs 传统线程 ~100MB+");
        System.out.println("  ✅ 启动速度: 虚拟线程几乎零开销创建");
        System.out.println("  ✅ 并发能力: 支持百万级并发任务");
        System.out.println("  ✅ 适用场景: IO密集型应用（Web服务、数据库操作）");
        System.out.println();
        System.out.println("传统线程池限制:");
        System.out.println("  ❌ 内存消耗: 每个线程栈 ~1MB");
        System.out.println("  ❌ 线程数量: 通常限制在几百个");
        System.out.println("  ❌ 上下文切换: 大量线程导致高切换开销");
    }

    public static void main(String[] args) {
        runPerformanceTest();
        
        // 可选：运行完整的大规模测试（警告：会很慢）
        if (args.length > 0 && "--full".equals(args[0])) {
            System.out.println("\n⚠️  运行完整测试，请耐心等待...\n");
            
            Result traditional = traditionalThreadPool();
            Result cached = cachedThreadPool();
            Result virtual = virtualThreads();
            
            System.out.println("\n=== 📊 完整测试结果 ===");
            System.out.println(traditional);
            System.out.println(cached);
            System.out.println(virtual);
        }
    }
}