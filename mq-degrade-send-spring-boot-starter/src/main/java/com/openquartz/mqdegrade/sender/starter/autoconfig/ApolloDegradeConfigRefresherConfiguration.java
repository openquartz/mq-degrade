package com.openquartz.mqdegrade.sender.starter.autoconfig;

import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.openquartz.mqdegrade.sender.common.alert.DegradeAlert;
import com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradeConfigProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Bean;

/**
 * Apollo 配置自动刷新
 *
 * @author svnee
 */
@EnableConfigurationProperties(DegradeConfigProperty.class)
@ConditionalOnClass(ApolloConfigChangeListener.class)
public class ApolloDegradeConfigRefresherConfiguration {

    @Bean
    public DegradeMessageApolloConfigRefresher degradeMessageApolloConfigRefresher(RefreshScope refreshScope,
                                                                                   DegradeConfigProperty degradeConfigProperty,
                                                                                   DegradeAlert degradeAlert) {
        return new DegradeMessageApolloConfigRefresher(refreshScope, degradeConfigProperty, degradeAlert);
    }
}
