package com.openquartz.mqdegrade.sender.common.alert;

/**
 * degrade alert
 * @author svnee
 */
public interface DegradeAlert {

    /**
     * send alert
     * @param content alert content
     */
    void alert(String content);
}
