package com.openquartz.mqdegrade.example.send.producer;

import com.openquartz.mqdegrade.sender.annotation.SendRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageProducer {

    /**
     * 降级传输到 “test" 的路由方法中。
     *
     * @param message 消息
     */
    @SendRouter(resource = "test")
    public boolean sendMessage(String message) {
        log.info("[MessageProducer#sendMessage] sendMessage:{}", message);
        return true;
    }

    public boolean sendMessage2(String message) {
        log.info("[MessageProducer#sendMessage2] sendMessage:{}", message);
        return true;
    }
}
