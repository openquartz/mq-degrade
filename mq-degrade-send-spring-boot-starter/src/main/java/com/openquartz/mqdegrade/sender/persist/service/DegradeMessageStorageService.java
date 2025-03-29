package com.openquartz.mqdegrade.sender.persist.service;

/**
 * 降级消息存储服务
 * @author svnee
 */
public interface DegradeMessageStorageService {

    /**
     * 存储消息
     * @param resource 降级资源
     * @param message 消息
     * @param key key
     */
    void save(String resource,String message, String key);

}
