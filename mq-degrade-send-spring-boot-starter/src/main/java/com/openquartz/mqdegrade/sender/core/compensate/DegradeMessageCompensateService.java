package com.openquartz.mqdegrade.sender.core.compensate;

import com.openquartz.mqdegrade.sender.core.compensate.condition.CompensateDegradeMessageCondition;

/**
 * 降级消息补偿服务
 *
 * @author svnee
 */
public interface DegradeMessageCompensateService {

    /**
     * 补偿
     */
    void compensate();

    /**
     * 按照ip补偿
     *
     * @param ip ip
     */
    void compensate(String ip);

    /**
     * 补偿预警
     */
    void compensateAlert();

    /**
     * 补偿降级消息
     *
     * @param condition 补偿条件
     */
    void compensate(CompensateDegradeMessageCondition condition);

    /**
     * 补偿预警
     *
     * @param degradeCondition 补偿预警条件
     */
    void compensateAlert(CompensateDegradeMessageCondition degradeCondition);
}
