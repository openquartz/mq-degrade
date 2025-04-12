package com.openquartz.mqdegrade.sender.starter.autoconfig.intercept;

import com.openquartz.mqdegrade.sender.annotation.SendRouter;
import com.openquartz.mqdegrade.sender.core.send.SendMessageFacade;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * SendRouterInterceptor
 *
 * @author svnee
 */
public class SendRouterInterceptor implements MethodInterceptor {

    private final SendMessageFacade sendMessageFacade;

    public SendRouterInterceptor(SendMessageFacade sendMessageFacade) {
        this.sendMessageFacade = sendMessageFacade;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        SendRouter sendRouter = AnnotationUtils.findAnnotation(invocation.getMethod(), SendRouter.class);
        if (sendRouter == null) {
            return invocation.proceed();
        }

        boolean send = sendMessageFacade.send(invocation.getArguments()[0], sendRouter.resource());
        if (invocation.getMethod().getReturnType().isAssignableFrom(Boolean.class)) {
            return send;
        }
        return null;
    }
}
