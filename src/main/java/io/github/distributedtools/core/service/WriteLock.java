package io.github.distributedtools.core.service;

import io.github.distributedtools.core.LockEntity;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class WriteLock implements Lock {

    private RLock rLock;

    private LockEntity lockEntity;

    private RedissonClient redissonClient;

    public WriteLock(LockEntity lockEntity, RedissonClient redissonClient) {
        this.lockEntity = lockEntity;
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean lock() {
        rLock = redissonClient.getReadWriteLock(lockEntity.getKey()).writeLock();
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
