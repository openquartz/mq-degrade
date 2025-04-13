package com.openquartz.mqdegrade.example.send.producer;

import com.openquartz.mqdegrade.sender.annotation.SendRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageProducer {

    @SendRouter(resource = "test")
    public void sendMessage(String message) {
        log.info("[MessageProducer#sendMessage] sendMessage:{}", message);
    }
}
