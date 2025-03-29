package com.openquartz.mqdegrade.sender.starter.autoconfig.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyBeanNameConstants.DEGRADE_CONFIG_PROPERTY_BEAN_NAME;
import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyPrefixConstants.CONFIG;

@Data
@Component(DEGRADE_CONFIG_PROPERTY_BEAN_NAME)
@ConfigurationProperties(prefix = CONFIG)
public class DegradeConfigProperty {

    /**
     * 配置中心namespace.如有多个需要用逗号分割。
     */
    private String namespace = "application";
}
