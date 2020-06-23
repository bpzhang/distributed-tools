package io.github.distributedtools.core.aop;

import io.github.distributedtools.annotation.Cacheable;
import io.github.distributedtools.annotation.DLock;
import io.github.distributedtools.core.LockEntity;
import io.github.distributedtools.core.service.Lock;
import io.github.distributedtools.core.strategy.LockStrategy;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.management.LockInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author bpzhang
 */
@Aspect
@Component
@Order
public class CacheAspect {

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser parser = new SpelExpressionParser();
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(cacheable)")
    public Object around(ProceedingJoinPoint joinPoint, Cacheable cacheable) {
        String[] cacheNames = cacheable.cacheNames();
        String[] values = cacheable.value();
        String key = getKey(joinPoint, cacheable);
        String unless = cacheable.unless();
        long expire = cacheable.expire();
        List<String> cacheNameList = Arrays.stream(cacheNames).map(cacheName -> cacheName + "::" + key).collect(Collectors.toList());
        Arrays.stream(values).map(value -> value + "::" + key).forEach(cacheNameList::add);
        try {

            for (String s : cacheNameList) {
                RBucket<Object> bucket = redissonClient.getBucket(s);
                Object o = bucket.get();
                if (o != null) {
                    return o;
                }
            }
            Object proceed = joinPoint.proceed();
            for (String s : cacheNameList) {
                RBucket<Object> bucket = redissonClient.getBucket(s);
                bucket.set(proceed);
                bucket.expire(expire, TimeUnit.SECONDS);
            }
            return proceed;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private String getKey(JoinPoint joinPoint, Cacheable cacheable) {
        Method method = getMethod(joinPoint);
        List<String> definitionKeys = getSpelDefinitionKey(cacheable.key(), method, joinPoint.getArgs());
        List<String> keyList = new ArrayList<>(definitionKeys);
        String key = StringUtils.collectionToDelimitedString(keyList, "", "", "");
        return key;
    }


    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    private List<String> getSpelDefinitionKey(String definitionKey, Method method, Object[] parameterValues) {
        List<String> definitionKeyList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(definitionKey)) {
            EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
            Object objKey = parser.parseExpression(definitionKey).getValue(context);
            definitionKeyList.add(ObjectUtils.nullSafeToString(objKey));
        }

        return definitionKeyList;
    }

}
