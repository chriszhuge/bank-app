package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionLoadTestWithReport {

    private static final int THREADS = 100;       // 并发线程数
    private static final int REQUESTS_PER_THREAD = 200;  // 每线程请求数

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        String jsonTemplate = """
        {
          "userName": "压测用户",
          "accountNumber": "622202020000000000",
          "amount": 100.00,
          "currency": "CNY",
          "status": "SUCCESS",
          "type": "DEPOSIT",
          "channel": "COUNTER",
          "description": "压力测试"
        }
        """;

        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger sentinalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            executor.submit(() -> {
                for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                    long reqStart = System.nanoTime();
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/transactions"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(jsonTemplate))
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        long reqEnd = System.nanoTime();

                        long durationMs = TimeUnit.NANOSECONDS.toMillis(reqEnd - reqStart);
                        responseTimes.add(durationMs);

                        if (response.body().contains("\"code\":0") ) {
                            successCount.incrementAndGet();
                        }else if(response.body().contains("\"code\":99999")){
                            sentinalCount.incrementAndGet();
                        }
                        else {
                            failCount.incrementAndGet();
                            System.err.println("请求失败: " + response.body());
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        System.err.println("请求异常: " + e.getMessage());
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();

        // 统计数据
        long totalRequests = THREADS * REQUESTS_PER_THREAD;
        long totalDuration = endTime - startTime;

        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.println("=== 压测报告 ===");
        System.out.println("总请求数: " + totalRequests);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("服务降级数：" + sentinalCount.get());
        System.out.println("失败请求数: " + failCount.get());
        System.out.println("总耗时(ms): " + totalDuration);
        System.out.printf("平均响应时间(ms): %.2f%n", avg);
        System.out.println("最大响应时间(ms): " + max);
        System.out.println("最小响应时间(ms): " + min);
        System.out.printf("吞吐率(请求/秒): %.2f%n", totalRequests / (totalDuration / 1000.0));
    }
}
