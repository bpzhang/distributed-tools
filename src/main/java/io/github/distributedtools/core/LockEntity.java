package io.github.distributedtools.core;

import io.github.distributedtools.enums.LockType;

/**
 * @author bpzhang
 */
public class LockEntity {

    private String key;

    private LockType lockType;

    private Long waitTime;

    private Long releaseTime;

    public LockEntity(String key, LockType lockType, Long waitTime, Long releaseTime) {
        this.key = key;
        this.lockType = lockType;
        this.waitTime = waitTime;
        this.releaseTime = releaseTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LockType getLockType() {
        return lockType;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public Long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Long waitTime) {
        this.waitTime = waitTime;
    }

    public Long getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Long releaseTime) {
        this.releaseTime = releaseTime;
    }
}
