package com.openquartz.mqdegrade.sender.core.send;

/**
 * MQ Message Send Facade
 *
 * @author svnee
 */
public interface SendMessageFacade {

    /**
     * send message.统一发送消息入口
     *
     * @param message message
     * @param resource message resource.发送消息资源标识。可以是topic等
     * @param <T> T message type
     * @return send result.true: send success,false: send fail
     */
    <T> boolean send(T message, String resource);


}
