package com.openquartz.mqdegrade.sender.starter.autoconfig.impl;

import com.openquartz.mqdegrade.sender.core.send.DegradeMessageFilter;
import com.openquartz.mqdegrade.sender.starter.autoconfig.property.DegradeFilterProperty;
import lombok.AllArgsConstructor;

import java.util.Objects;

/**
 * 支持资源维度的降级消息过滤
 * 可替换实现
 */
@AllArgsConstructor
public class DefaultDegradeMessageFilterImpl implements DegradeMessageFilter {

    private final DegradeFilterProperty degradeFilterProperty;

    @Override
    public boolean filter(Object degradeMsgEntity, String resource) {
        return Objects.equals(Boolean.TRUE, degradeFilterProperty.getResourceFilter().get(resource));
    }
}
