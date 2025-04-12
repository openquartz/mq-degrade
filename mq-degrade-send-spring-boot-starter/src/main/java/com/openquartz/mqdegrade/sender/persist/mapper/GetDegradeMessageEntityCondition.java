package com.openquartz.mqdegrade.sender.persist.mapper;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
public class GetDegradeMessageEntityCondition {

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

    /**
     * 最小值
     */
    private Long minId;
}
