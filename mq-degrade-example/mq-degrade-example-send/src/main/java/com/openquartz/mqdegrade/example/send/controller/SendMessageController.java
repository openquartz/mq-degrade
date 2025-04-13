package com.openquartz.mqdegrade.example.send.controller;

import com.openquartz.mqdegrade.example.send.producer.MessageProducer;
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

    @GetMapping("/send")
    public void send(@RequestParam("message") String message) {
        messageProducer.sendMessage(message);
    }
}
