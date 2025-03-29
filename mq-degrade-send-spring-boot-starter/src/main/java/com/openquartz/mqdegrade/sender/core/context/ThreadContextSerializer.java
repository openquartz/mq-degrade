package com.openquartz.mqdegrade.sender.core.context;

/**
 * ThreadContextSerializer
 * @author svnee
 */
public interface ThreadContextSerializer {

    /**
     * 序列化Thread上下文
     * @return 序列化结果
     */
    String serializeContext();

    /**
     * 反序列上下文
     * 执行序列号前建议先清理一下上下文，否则可能影响上下文数据影响
     * @param context context
     */
    void deserializeContext(String context);
}
