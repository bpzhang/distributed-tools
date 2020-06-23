package io.github.distributedtools.annotation;

import io.github.distributedtools.enums.LockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bpzhang
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface DLock {

    String name() default "";

    long waitTime() default -1;

    long releaseTime() default -1;

    LockType lockType() default LockType.REENTRANT;

}
