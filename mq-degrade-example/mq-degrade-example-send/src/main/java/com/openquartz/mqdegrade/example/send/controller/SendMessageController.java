package com.openquartz.mqdegrade.example.send.controller;

import com.openquartz.mqdegrade.example.send.constants.MQResourceConstant;
import com.openquartz.mqdegrade.example.send.producer.MessageProducer;
import com.openquartz.mqdegrade.sender.core.send.SendMessageFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/message")
public class SendMessageController {

    @Resource
    private MessageProducer messageProducer;
    @Resource
    private SendMessageFacade sendMessageFacade;

    @GetMapping("/send1")
    public void send(@RequestParam("message") String message) {
        messageProducer.sendMessage(message);
    }

    @GetMapping("/send2")
    public void send2(@RequestParam("message") String message) {
        sendMessageFacade.send(MQResourceConstant.TEST_RESOURCE_2, message);
    }

}
