package io.github.distributedtools;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DistributedToolsApplicationTests.class)
public class LockTest {

    @Autowired
    TestService testService;

    @Test
    public void multithreadingTest() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        IntStream.range(0, 10).forEach(i -> executorService.submit(() -> {
            try {
                String result = testService.getValue("sleep");
                System.out.println("线程:[" + Thread.currentThread().getName() + "]拿到结果=》" + result + new Date().toLocaleString());
            } catch (Exception e) {
               System.err.println(e+""+ new Date().toLocaleString());
            }
        }));
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }

}
