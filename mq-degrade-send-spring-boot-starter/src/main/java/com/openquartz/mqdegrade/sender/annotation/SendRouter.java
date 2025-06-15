package com.openquartz.mqdegrade.sender.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 发送路由方法标记
 *
 * <p>使用样例：
 * <pre>
 *
 *  &#064;Component
 *  class Test1 {
 *
 *     &#064;SendRouter(resource = "SendTest")
 *     public boolean send(String msg) {
 *         // TODO 发送消息
 *         return true;
 *     }
 *  }
 *
 * </pre>
 * </p>
 *
 * @author svnee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SendRouter {

    /**
     * 发送绑定资源
     *
     * @return resource
     */
    String resource();
}