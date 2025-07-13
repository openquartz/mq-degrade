package com.openquartz.mqdegrade.example.send.config;

import com.openquartz.mqdegrade.example.send.producer.DegradeMessageManager;
import com.openquartz.mqdegrade.example.send.producer.MessageProducer;
import com.openquartz.mqdegrade.sender.core.factory.DegradeTransferBindingConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.openquartz.mqdegrade.example.send.constants.MQResourceConstant.*;

/**
 * 手动绑定降级传输配置
 *
 * @author svnee
 */
@Component
public class DegradeBindingConfig implements InitializingBean {

    @Resource
    private MessageProducer messageProducer;
    @Resource
    private DegradeMessageManager degradeMessageManager;

    @Override
    public void afterPropertiesSet() {

        DegradeTransferBindingConfig
                .builder(TEST_RESOURCE_2)
                // 直接发送
                .send(String.class, messageProducer::sendMessage2)
                // 第一个消费分组降级传输
                .degrade(TEST2_DEGRADE_RESOURCE_1, String.class, msg -> {
                    degradeMessageManager.degradeTransfer2(msg);
                    return true;
                })
                // 第二个消费分组降级传输
                .degrade(TEST2_DEGRADE_RESOURCE_2, String.class, msg -> {
                    degradeMessageManager.degradeTransfer3(msg);
                    return true;
                })
                .binding();
    }
}
