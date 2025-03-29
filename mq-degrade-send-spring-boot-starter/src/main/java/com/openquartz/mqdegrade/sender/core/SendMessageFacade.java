package com.openquartz.mqdegrade.sender.core;

/**
 * MQ Message Send Facade
 *
 * @author svnee
 */
public interface SendMessageFacade {

    /**
     * send message
     *
     * @param message message
     * @param resource message resource
     * @param <T> T
     * @return send result
     */
    <T> boolean send(T message, String resource);


}
