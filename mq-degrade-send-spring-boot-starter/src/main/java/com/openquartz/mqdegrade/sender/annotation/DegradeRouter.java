package com.openquartz.mqdegrade.sender.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 降级传输路由
 *
 * <p>使用样例：
 * <pre>
 *     &#064;DegradeRouter(resource = "SendTest",degradeResource = "test2_group1")
 *      public boolean send(String msg) {
 *          // TODO 发送消息
 *          return true;
 *      }
 * </pre>
 *</p>
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

    /**
     * 降级资源
     *
     * @return 降级-resource
     */
    String degradeResource();
}
