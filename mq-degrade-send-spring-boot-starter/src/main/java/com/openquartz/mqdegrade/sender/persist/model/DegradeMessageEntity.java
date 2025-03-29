package com.openquartz.mqdegrade.sender.persist.model;

import lombok.Data;

import java.util.Date;

@Data
public class DegradeMessageEntity {

    /**
     * id
     */
    private Long id;

    /**
     * 资源
     */
    private String resource;

    /**
     * 发送消息
     */
    private String message;

    /**
     * 消息key
     */
    private String msgKey;

    /**
     * 上下文
     */
    private String context;

    /**
     * ip 地址
     */
    private String ipAddr;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最新重试时间
     */
    private Date lastRetryTime;

}
