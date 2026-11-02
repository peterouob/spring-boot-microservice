package com.peter.user_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Pointcut("execution (* com.peter.user_service.controller..*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        }finally {
            long end = System.nanoTime();
            long elapsedNS = end - start;
            long elapsedMS = TimeUnit.MILLISECONDS.convert(elapsedNS, TimeUnit.NANOSECONDS);
            String signature = pjp.getSignature().getName();
            log.info("Controller method {} execution in {} ms", signature, elapsedMS);
        }
    }
}
