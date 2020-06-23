package io.github.distributedtools.annotation;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cacheable {

    @AliasFor("cacheNames")
    String[] value() default {};


    @AliasFor("value")
    String[] cacheNames() default {};


    String key() default "";

    String unless() default "";

    long expire() default -1;

}
