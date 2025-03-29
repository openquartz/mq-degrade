package com.openquartz.mqdegrade.sender.common.alert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultDegradeAlertImpl implements DegradeAlert {

    @Override
    public void alert(String content) {
        log.warn("[DefaultDegradeAlertImpl#alert] Degrade Alert!content:{}", content);
    }
}
