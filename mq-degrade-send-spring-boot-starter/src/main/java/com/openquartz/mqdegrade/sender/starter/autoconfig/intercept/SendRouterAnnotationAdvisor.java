package com.openquartz.mqdegrade.sender.starter.autoconfig.intercept;

import com.openquartz.mqdegrade.sender.annotation.SendRouter;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
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
        return AnnotationMatchingPointcut.forMethodAnnotation(SendRouter.class);
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
