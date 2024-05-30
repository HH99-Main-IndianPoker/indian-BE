package com.service.indianfrog.domain.game.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private final MeterRegistry registry;

    public PerformanceAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    @Around("execution(* com.service.indianfrog.domain.gameroom.service.GameRoomService.*(..))")
    public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Timer.Sample sample = Timer.start(registry);

        try {
            Object result = joinPoint.proceed();
            return result;
        }finally {
            sample.stop(registry.timer("method.execution.time", "method", methodName));
        }
    }
}
