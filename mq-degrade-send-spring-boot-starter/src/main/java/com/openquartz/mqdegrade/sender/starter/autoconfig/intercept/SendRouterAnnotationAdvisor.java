package com.openquartz.mqdegrade.sender.starter.autoconfig.intercept;

import java.lang.reflect.Method;

import com.openquartz.mqdegrade.sender.annotation.SendRouter;
import org.aopalliance.aop.Advice;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.NonNull;

/**
 * SendRouterAdvisor
 *
 * @author svnee
 */
public class SendRouterAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    private final transient Advice advice;
    private final transient Pointcut pointcut;

    public SendRouterAnnotationAdvisor(SendRouterInterceptor interceptor) {
        this.advice = interceptor;
        this.pointcut = buildPointcut();
    }

    /**
     * 所有加上SendRouter注解的方法的切面点
     *
     * @return 切面点
     */
    private Pointcut buildPointcut() {

        // 创建一个匹配带有SendRouter注解的方法的切点
        Pointcut methodPointcut = new AnnotationMatchingPointcut(null, SendRouter.class, true);

        // 创建一个可组合的切点
        ComposablePointcut result = new ComposablePointcut(methodPointcut);

        // 添加一个静态方法匹配器，用于进一步过滤方法
        result.intersection(new MethodMatcher() {
            @Override
            public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
                return method.isAnnotationPresent(SendRouter.class);
            }

            @Override
            public boolean isRuntime() {
                return false;
            }

            @Override
            public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass, @NonNull Object... args) {
                return method.isAnnotationPresent(SendRouter.class);
            }
        });

        return result;
    }

    @Override
    @NonNull
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    @NonNull
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }
}
