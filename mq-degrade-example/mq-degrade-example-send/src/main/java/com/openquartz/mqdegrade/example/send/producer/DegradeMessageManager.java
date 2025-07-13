package com.openquartz.mqdegrade.example.send.producer;

import com.openquartz.mqdegrade.sender.annotation.DegradeRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.openquartz.mqdegrade.example.send.constants.MQResourceConstant.TEST_DEGRADE_RESOURCE;
import static com.openquartz.mqdegrade.example.send.constants.MQResourceConstant.TEST_RESOURCE;

@Slf4j
@Component
public class DegradeMessageManager {

    @DegradeRouter(resource = TEST_RESOURCE, degradeResource = TEST_DEGRADE_RESOURCE)
    public void degradeTransfer(String degradeMessage) {
        log.info("[DegradeMessageManager#degradeTransfer] degrade-message:{}", degradeMessage);
    }

    public void degradeTransfer2(String degradeMessage) {
        log.info("[DegradeMessageManager#degradeTransfer2] degrade-message:{}", degradeMessage);
    }

    public void degradeTransfer3(String degradeMessage) {
        log.info("[DegradeMessageManager#degradeTransfer3] degrade-message:{}", degradeMessage);
    }
}
