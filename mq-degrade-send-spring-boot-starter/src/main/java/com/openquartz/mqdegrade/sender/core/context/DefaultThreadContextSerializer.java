package com.openquartz.mqdegrade.sender.core.context;

/**
 * 默认序列化器
 * @author svnee
 */
public class DefaultThreadContextSerializer implements ThreadContextSerializer {

    @Override
    public String serializeContext() {
        return "";
    }

    @Override
    public void deserializeContext(String context) {

    }
}
