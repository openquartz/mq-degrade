package com.openquartz.mqdegrade.sender.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 发送路由
 *
 * @author svnee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DegradeRouter {

    /**
     * 发送绑定资源
     *
     * @return resource
     */
    String resource();
}
