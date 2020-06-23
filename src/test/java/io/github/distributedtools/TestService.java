package io.github.distributedtools;

import io.github.distributedtools.annotation.Cacheable;
import io.github.distributedtools.annotation.DLock;
import org.springframework.stereotype.Service;

/**
 * Created by kl on 2017/12/29.
 */
@Service
public class TestService {

    @DLock(waitTime = 3,releaseTime = 60,name = "#param")
    public String getValue(String param) throws Exception {
      //  if ("sleep".equals(param)) {//线程休眠或者断点阻塞，达到一直占用锁的测试效果
            Thread.sleep(1000*20);
        //}
        return "success";
    }


    @Cacheable(value = "cache-name",key = "#param",expire = 200)
    public String getValueE(String param) throws Exception {
      //  if ("sleep".equals(param)) {//线程休眠或者断点阻塞，达到一直占用锁的测试效
        //}
        System.out.println("entry");
        return "success";
    }



}
