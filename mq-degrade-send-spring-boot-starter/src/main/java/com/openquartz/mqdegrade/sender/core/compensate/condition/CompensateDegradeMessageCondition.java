package com.openquartz.mqdegrade.sender.core.compensate.condition;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

/**
 * 降级消息补偿条件
 *
 * @author svnee
 */
@Getter
@Builder
public class CompensateDegradeMessageCondition {

    /**
     * 创建时间开始-开始
     */
    private Date startCreateTime;

    /**
     * 创建时间范围-结束
     */
    private Date endCreateTime;

    /**
     * 资源列表
     */
    private List<String> resourceList;

    /**
     * 消息key
     */
    private List<String> msgKeyList;

    /**
     * IP地址列表
     */
    private List<String> ipList;

    /**
     * 步长，单位分钟
     */
    private Integer stepMinutes;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
}
