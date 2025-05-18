package com.openquartz.mqdegrade.example.send.producer;

import com.openquartz.mqdegrade.sender.annotation.DegradeRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DegradeMessageManager {

    @DegradeRouter(resource = "test", degradeResource = "test_group1")
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
