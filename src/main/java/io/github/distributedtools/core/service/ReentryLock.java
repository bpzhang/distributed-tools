package io.github.distributedtools.core.service;

import io.github.distributedtools.core.LockEntity;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author bpzhang
 */
public class ReentryLock implements Lock {
    private RLock rLock;

    private final LockEntity lockEntity;

    private final RedissonClient redissonClient;

    public ReentryLock(LockEntity lockEntity, RedissonClient redissonClient) {
        this.lockEntity = lockEntity;
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean lock() {
        rLock = redissonClient.getLock(lockEntity.getKey());
        try {
            return rLock.tryLock(lockEntity.getWaitTime(), lockEntity.getReleaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        if (rLock.isHeldByCurrentThread()) {
            return rLock.forceUnlock();
        }
        return false;
    }
}
