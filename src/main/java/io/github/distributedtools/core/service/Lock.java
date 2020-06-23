package io.github.distributedtools.core.service;

/**
 * @author bpzhang
 */
public interface Lock {

    boolean lock();

    boolean release();
}
