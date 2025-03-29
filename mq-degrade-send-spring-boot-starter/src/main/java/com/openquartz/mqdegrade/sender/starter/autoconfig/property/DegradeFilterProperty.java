package com.openquartz.mqdegrade.sender.starter.autoconfig.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyBeanNameConstants.DEGRADE_FILTER_PROPERTY_BEAN_NAME;
import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyPrefixConstants.FILTER;

@Data
@RefreshScope
@Component(DEGRADE_FILTER_PROPERTY_BEAN_NAME)
@ConfigurationProperties(prefix = FILTER)
public class DegradeFilterProperty {

    /**
     * 资源过滤配置
     * key: 资源，value: 是否过滤
     */
    private Map<String, Boolean> resourceFilter = new HashMap<>();

}
