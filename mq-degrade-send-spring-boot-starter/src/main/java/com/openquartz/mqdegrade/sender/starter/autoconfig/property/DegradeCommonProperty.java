package com.openquartz.mqdegrade.sender.starter.autoconfig.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyPrefixConstants.COMMON;

/**
 * @author svnee
 */
@Data
@RefreshScope
@Component(DegradePropertyBeanNameConstants.DEGRADE_COMMON_PROPERTY_BEAN_NAME)
@ConfigurationProperties(prefix = COMMON)
public class DegradeCommonProperty {

    /**
     * 是否开启
     */
    private boolean enable = true;

    /**
     * 是否开启强制降级
     */
    private boolean enableForceDegrade = false;

    /**
     * 是否开启自动降级
     */
    private boolean enableAutoDegrade = false;

    /**
     * 资源级降级配置
     */
    private Map<String, ResourceDegradeProperty> resourceDegrade;

    public Map<String, ResourceDegradeProperty> getResourceDegrade() {
        return Optional.ofNullable(resourceDegrade).orElse(Collections.emptyMap());
    }

    @Data
    public static class ResourceDegradeProperty {

        /**
         * 是否开启强制降级
         */
        private boolean enableForceDegrade;

        /**
         * 是否开启自动降级
         */
        private boolean enableAutoDegrade;

        /**
         * 自动降级SentinelResource资源名称
         */
        private String autoDegradeSentinelResource;

        /**
         * 是否开启并行降级传输。默认串行降级传输
         */
        private boolean enableParallelDegradeTransfer;
    }

}

