package com.openquartz.mqdegrade.sender.starter.autoconfig.processor;

import com.openquartz.mqdegrade.sender.annotation.DegradeRouter;
import com.openquartz.mqdegrade.sender.common.exception.DegradeException;
import com.openquartz.mqdegrade.sender.common.exception.ExceptionUtils;
import com.openquartz.mqdegrade.sender.core.factory.DegradeRouterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * MQ降级注解处理器
 */
@Slf4j
public class DegradeRouterAnnotationProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, method -> {
            DegradeRouter degradeRouter = AnnotationUtils.findAnnotation(method, DegradeRouter.class);
            if (degradeRouter != null) {
                processMethod(bean, method, degradeRouter);
            }
        });
        return bean;
    }

    private void processMethod(Object bean, Method method, DegradeRouter degradeRouter) {

        // 要求method 的入参只有一个
        if (method.getParameterTypes().length != 1) {
            log.error("[DegradeRouterAnnotationProcessor#processMethod] bean method:{} param length must be 1!", method.getName());
            throw new DegradeException(String.format("resource:%s,method:%s param length must be 1", degradeRouter.resource(), method.getName()));
        }

        if (!(method.getReturnType().isAssignableFrom(Void.class) && method.getReturnType().isAssignableFrom(Boolean.class))) {
            log.error("[DegradeRouterAnnotationProcessor#processMethod] bean method:{} return type must be boolean or void!", method.getName());
            throw new DegradeException(String.format("resource:%s,method:%s return type must be boolean or void", degradeRouter.resource(), method.getName()));
        }

        DegradeRouterFactory.register(degradeRouter.resource(), method.getParameterTypes()[0], inv -> {
            try {
                if (method.getReturnType().isAssignableFrom(Boolean.class)) {
                    return (boolean) method.invoke(bean, inv);
                }
                method.invoke(bean, inv);
                return true;
            } catch (Exception ex) {
                log.error("[DegradeRouterAnnotationProcessor#processMethod] method:{} invoke error!", method.getName());
                return ExceptionUtils.wrapAndThrow(ex);
            }
        });
    }
}