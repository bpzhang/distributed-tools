package io.github.distributedtools.core.service;

import io.github.distributedtools.core.LockEntity;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class FairLock implements Lock {

    private RLock rLock;

    private LockEntity lockEntity;

    private RedissonClient redissonClient;

    public FairLock(LockEntity lockEntity, RedissonClient redissonClient) {
        this.lockEntity = lockEntity;
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean lock() {
         rLock = redissonClient.getFairLock(lockEntity.getKey());
        try {
            return rLock.tryLock(lockEntity.getWaitTime(),lockEntity.getReleaseTime(), TimeUnit.SECONDS);
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
