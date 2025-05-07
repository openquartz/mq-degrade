package com.openquartz.mqdegrade.sender.core.factory;

import com.openquartz.mqdegrade.sender.common.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 绑定降级传输配置
 *
 * @author svnee
 */
public class DegradeTransferBindingConfig {

    @SuppressWarnings("all")
    public static class DegradeTransferBindingConfigBuilder {

        private final String resource;

        private Pair<Class, Predicate> sendPair;

        private List<Pair<Class, Predicate>> degradeList = new LinkedList<>();

        private DegradeTransferBindingConfigBuilder(String resource) {
            this.resource = resource;
        }

        /**
         * 直接发送配置
         * @param messageClazz 消息类型
         * @param sendPredicate 发送函数
         * @return builder
         * @param <T> T 消息类
         */
        public <T> DegradeTransferBindingConfigBuilder send(Class<T> messageClazz, Predicate<T> sendPredicate) {
            assert messageClazz != null;
            assert sendPredicate != null;
            this.sendPair = Pair.of(messageClazz, sendPredicate);
            return this;
        }

        /**
         * 降级传输配置
         * @param messageClazz 降级传输类
         * @param degradePredicate 降级传输函数
         * @return builder
         * @param <T> T 降级传输消息类
         */
        public <T> DegradeTransferBindingConfigBuilder degrade(Class<T> messageClazz, Predicate<T> degradePredicate) {
            assert messageClazz != null;
            assert degradePredicate != null;
            this.degradeList.add(Pair.of(messageClazz, degradePredicate));
            return this;
        }

        /**
         * 绑定配置
         */
        public void binding() {

            assert resource != null;
            assert sendPair != null;
            assert degradeList != null && !degradeList.isEmpty();

            SendRouterFactory.register(resource, sendPair.getKey(), sendPair.getValue());

            for (Pair<Class, Predicate> degradeFunc : degradeList) {
                DegradeRouterFactory.register(resource, degradeFunc.getKey(), degradeFunc.getValue());
            }

        }
    }

    public static DegradeTransferBindingConfigBuilder builder(String resource) {
        return new DegradeTransferBindingConfigBuilder(resource);
    }
}
