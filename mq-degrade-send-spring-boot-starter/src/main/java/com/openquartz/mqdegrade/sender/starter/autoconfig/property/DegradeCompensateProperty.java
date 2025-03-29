package com.openquartz.mqdegrade.sender.starter.autoconfig.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyBeanNameConstants.DEGRADE_COMPENSATE_PROPERTY_BEAN_NAME;
import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyPrefixConstants.COMPENSATE;

/**
 * @author svnee
 */
@Data
@RefreshScope
@Component(DEGRADE_COMPENSATE_PROPERTY_BEAN_NAME)
@ConfigurationProperties(prefix = COMPENSATE)
public class DegradeCompensateProperty {

    /**
     * 是否开启补偿
     */
    private boolean enable = false;

    /**
     * 是否开启预警补偿
     */
    private boolean alertEnable = true;

    /**
     * 补偿策略最大重试次数
     */
    private Integer maxRetryCount = 15;

    /**
     * 补偿策略一次限制条数
     */
    private Integer limitCount = 10;

    /**
     * 全局补偿配置
     */
    private DegradeCompensateScheduleProperty global = new DegradeCompensateScheduleProperty(3600,0,600);

    /**
     * 本机补偿配置
     */
    private DegradeCompensateScheduleProperty self = new DegradeCompensateScheduleProperty(600,0,600);

    /**
     * 预警补偿配置
     */
    private DegradeCompensateScheduleProperty alert = new DegradeCompensateScheduleProperty(3600,60,600);

    @Data
    public static class DegradeCompensateScheduleProperty {

        /**
         * 获取当前机器执行补偿间隔时间，单位：s
         */
        private Integer backoffIntervalTime;

        /**
         * 补偿策略延迟时间
         */
        private long delayTime;

        /**
         * 补偿周期时间,单位：s
         */
        private long periodTime;

        public DegradeCompensateScheduleProperty(Integer backoffIntervalTime, long delayTime, long periodTime) {
            this.backoffIntervalTime = backoffIntervalTime;
            this.delayTime = delayTime;
            this.periodTime = periodTime;
        }
    }
}
