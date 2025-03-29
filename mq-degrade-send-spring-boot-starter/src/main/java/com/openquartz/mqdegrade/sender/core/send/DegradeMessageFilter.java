package com.openquartz.mqdegrade.sender.core.send;

/**
 * 降级消息过滤，支持降级消息不存储
 */
public interface DegradeMessageFilter {

    /**
     * 是否过滤降级消息
     * @param degradeMsgEntity 降级消息实体
     * @param resource 资源标识
     * @return true:过滤,false:不过滤
     */
    boolean filter(Object degradeMsgEntity, String resource);
}
