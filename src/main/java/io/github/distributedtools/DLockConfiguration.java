package io.github.distributedtools;

import io.github.distributedtools.config.DLockConfig;
import io.github.distributedtools.core.aop.DLockAspect;
import io.github.distributedtools.core.strategy.LockStrategy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author bpzhang
 */
@Configuration
@Import({DLockAspect.class})
public class DLockConfiguration {

    @Bean
    public LockStrategy lockFactory(){
        return new LockStrategy();
    }
    @Bean
    public DLockConfig lockConfig(){
        return new DLockConfig();
    }
}
