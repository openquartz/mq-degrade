package com.openquartz.mqdegrade.example.send.config;

import com.openquartz.mqdegrade.example.send.producer.DegradeMessageManager;
import com.openquartz.mqdegrade.example.send.producer.MessageProducer;
import com.openquartz.mqdegrade.sender.core.factory.DegradeTransferBindingConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DegradeBindingConfig implements InitializingBean {

    @Resource
    private MessageProducer messageProducer;
    @Resource
    private DegradeMessageManager degradeMessageManager;

    @Override
    public void afterPropertiesSet() {

        DegradeTransferBindingConfig
                .builder("test2")
                // 直接发送
                .send(String.class, messageProducer::sendMessage2)
                // 第一个消费分组降级传输
                .degrade("test2_group1", String.class, msg -> {
                    degradeMessageManager.degradeTransfer2(msg);
                    return true;
                })
                // 第二个消费分组降级传输
                .degrade("test2_group2", String.class, msg -> {
                    degradeMessageManager.degradeTransfer3(msg);
                    return true;
                })
                .binding();
    }
}
