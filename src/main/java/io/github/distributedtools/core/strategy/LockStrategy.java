package io.github.distributedtools.core.strategy;

import io.github.distributedtools.core.LockEntity;
import io.github.distributedtools.core.service.FairLock;
import io.github.distributedtools.core.service.Lock;
import io.github.distributedtools.core.service.ReadLock;
import io.github.distributedtools.core.service.RedLock;
import io.github.distributedtools.core.service.ReentryLock;
import io.github.distributedtools.core.service.WriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import static io.github.distributedtools.enums.LockType.REENTRANT;

/**
 * @author bpzhang
 */
public class LockStrategy {

    @Autowired
    private RedissonClient redissonClient;

    public Lock getLock(LockEntity lockEntity) {
        switch (lockEntity.getLockType()) {
            case FAIR:
                return new FairLock(lockEntity, redissonClient);
            case READ:
                return new ReadLock(lockEntity, redissonClient);
            case WRITE:
                return new WriteLock(lockEntity, redissonClient);
            case RED:
                return new RedLock(lockEntity, redissonClient);
            default:
                return new ReentryLock(lockEntity, redissonClient);
        }
    }
}
