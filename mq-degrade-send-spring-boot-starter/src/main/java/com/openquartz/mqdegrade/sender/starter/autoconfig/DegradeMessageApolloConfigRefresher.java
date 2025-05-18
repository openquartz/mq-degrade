package com.openquartz.mqdegrade.sender.starter.autoconfig;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.openquartz.mqdegrade.sender.common.alert.DegradeAlert;
import com.openquartz.mqdegrade.sender.common.utils.IpUtils;
import com.openquartz.mqdegrade.sender.common.utils.StringUtils;
import com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradeConfigProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyBeanNameConstants.*;
import static com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradePropertyPrefixConstants.*;

/**
 * ApolloConfigRefresher
 *
 * @author svnee
 */
@Slf4j
public class DegradeMessageApolloConfigRefresher implements EnvironmentAware {

    private final RefreshScope refreshScope;
    private final DegradeConfigProperty degradeConfigProperty;
    private final DegradeAlert degradeAlert;
    private Environment environment;

    public DegradeMessageApolloConfigRefresher(RefreshScope refreshScope, DegradeConfigProperty degradeConfigProperty, DegradeAlert degradeAlert) {
        this.refreshScope = refreshScope;
        this.degradeConfigProperty = degradeConfigProperty;
        this.degradeAlert = degradeAlert;
    }

    private static final Map<String, String> PROPERTY_PREFIX_2_BEAN_NAME_MAP = new HashMap<>();

    static {
        PROPERTY_PREFIX_2_BEAN_NAME_MAP.put(COMMON, DEGRADE_COMMON_PROPERTY_BEAN_NAME);
        PROPERTY_PREFIX_2_BEAN_NAME_MAP.put(FILTER, DEGRADE_FILTER_PROPERTY_BEAN_NAME);
        PROPERTY_PREFIX_2_BEAN_NAME_MAP.put(COMPENSATE, DEGRADE_COMPENSATE_PROPERTY_BEAN_NAME);
        PROPERTY_PREFIX_2_BEAN_NAME_MAP.put(TRANSFER, DEGRADE_PARALLEL_TRANSFER_PROPERTY_BEAN_NAME);
    }

    private static final String DEGRADE_CONFIG_REFRESH_ERROR_MESSAGE_TEMPLATE = "Degrade config refresh error!ip: %s,Bean: %s";

    @PostConstruct
    public void init() {

        String namespace = degradeConfigProperty.getNamespace();
        if (namespace == null || StringUtils.isBlank(namespace)) {
            return;
        }

        String[] namespaceArr = namespace.split(",");
        ConfigChangeListener configChangeListener = this::onChange;
        for (String namespaceName : namespaceArr) {
            if (StringUtils.isBlank(namespaceName)) {
                continue;
            }

            String resolvedNamespace = this.environment.resolvePlaceholders(namespaceName.trim());
            Config config = ConfigService.getConfig(resolvedNamespace);
            config.addChangeListener(configChangeListener);
        }

    }

    private void onChange(ConfigChangeEvent configChangeEvent) {

        if (configChangeEvent.changedKeys().stream().noneMatch(changeKey -> changeKey.startsWith(PREFIX))) {
            return;
        }

        PROPERTY_PREFIX_2_BEAN_NAME_MAP.forEach((key, value) -> {
            if (configChangeEvent.changedKeys().stream().anyMatch(changeKey -> changeKey.startsWith(key))) {
                try {
                    refreshScope.refresh(value);
                } catch (Exception ex) {
                    log.error("[DegradeMessageApolloConfigRefresher#onChange] Failed to refresh degrade config!", ex);
                    degradeAlert.alert(String.format(DEGRADE_CONFIG_REFRESH_ERROR_MESSAGE_TEMPLATE, IpUtils.getIp(), value));
                }
            }
        });
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }
}
