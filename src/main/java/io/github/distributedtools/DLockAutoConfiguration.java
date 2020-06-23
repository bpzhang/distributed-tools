package io.github.distributedtools;

import io.github.distributedtools.config.DLockConfig;
import io.github.distributedtools.core.aop.DLockAspect;
import io.github.distributedtools.core.strategy.LockStrategy;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;


/**
 * @author bpzhang
 */
@Configuration
@ConditionalOnProperty(prefix = DLockConfig.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DLockConfig.class)
@Import({DLockAspect.class})
public class DLockAutoConfiguration {

    @Autowired
    private DLockConfig lockConfig;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws Exception {
        Config config = new Config();
        if (lockConfig.getClusterServer() != null) {
            config.useClusterServers().setPassword(lockConfig.getPassword())
                    .addNodeAddress(lockConfig.getClusterServer().getNodeAddresses());
        } else {
            config.useSingleServer().setAddress(lockConfig.getAddress())
                    .setDatabase(lockConfig.getDatabase())
                    .setPassword(lockConfig.getPassword());
        }
        Codec codec = (Codec) ClassUtils.forName(lockConfig.getCodec(), ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public LockStrategy lockStrategy() {
        return new LockStrategy();
    }
}
