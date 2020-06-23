package io.github.distributedtools.core.aop;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.management.LockInfo;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bpzhang
 */
@Aspect
@Component
@Order
public class DLockAspect {
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser parser = new SpelExpressionParser();
    @Autowired
    private LockStrategy lockStrategy;
    private final Map<String, LockInfo> currentThreadLock = new ConcurrentHashMap<>();

    @Around("@annotation(dLock)")
    public Object around(ProceedingJoinPoint joinPoint, DLock dLock) throws Throwable {
        String currentLockKey = getCurrentLockKey(joinPoint, dLock);
        String currentLockId = currentLockKey + Thread.currentThread().getId();
        LockEntity entity = new LockEntity(currentLockKey, dLock.lockType(), dLock.waitTime(), dLock.releaseTime());
        currentThreadLock.put(currentLockId, new LockInfo(entity, false));
        Lock lock = lockStrategy.getLock(entity);
        if (lock.lock()) {
            currentThreadLock.get(currentLockId).setLock(lock);
            currentThreadLock.get(currentLockId).setLocked(true);
            return joinPoint.proceed();
        } else {
            throw new RuntimeException("can not lock:" + currentLockId);
        }
    }

    @AfterReturning(value = "@annotation(lock)")
    public void afterReturning(JoinPoint joinPoint, DLock lock) {
        String currentLockKey = getCurrentLockKey(joinPoint, lock);
        String currentLockId = currentLockKey + Thread.currentThread().getId();
        unlock(currentLockId);
    }

    @AfterThrowing(value = "@annotation(lock)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, DLock lock, Throwable ex) throws Throwable {
        String currentLockKey = getCurrentLockKey(joinPoint, lock);
        String currentLockId = currentLockKey + Thread.currentThread().getId();
        unlock(currentLockId);
        throw ex;
    }

    private void unlock(String currentLock) {
        LockInfo lockRes = currentThreadLock.get(currentLock);
        if (Objects.isNull(lockRes)) {
            throw new NullPointerException("Please check whether the input parameter used as the lock key value has " +
                    "been modified in the method, which will cause the acquire and release locks to have different key values and throw null pointers.curentLockKey:" + currentLock);
        }
        if (lockRes.getLocked()) {
            boolean releaseRes = currentThreadLock.get(currentLock).getLock().release();
            // avoid release lock twice when exception happens below
            lockRes.setLocked(false);
            if (releaseRes) {
                currentThreadLock.remove(currentLock);
            }
        }
    }

    private String getCurrentLockKey(JoinPoint joinPoint, DLock lock) {
        Method method = getMethod(joinPoint);
        List<String> definitionKeys = getSpelDefinitionKey(lock.name(), method, joinPoint.getArgs());
        List<String> keyList = new ArrayList<>(definitionKeys);
        String key = StringUtils.collectionToDelimitedString(keyList, "", "-", "");
        return "lock:" + key;
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

    private static class LockInfo {

        private LockEntity lockEntity;
        private Lock lock;
        private Boolean isLocked;

        public LockInfo(LockEntity lockEntity, Boolean isLocked) {
            this.lockEntity = lockEntity;
            this.isLocked = isLocked;
        }

        public LockEntity getLockEntity() {
            return lockEntity;
        }

        public void setLockEntity(LockEntity lockEntity) {
            this.lockEntity = lockEntity;
        }

        public Lock getLock() {
            return lock;
        }

        public void setLock(Lock lock) {
            this.lock = lock;
        }

        public Boolean getLocked() {
            return isLocked;
        }

        public void setLocked(Boolean locked) {
            isLocked = locked;
        }
    }
}
