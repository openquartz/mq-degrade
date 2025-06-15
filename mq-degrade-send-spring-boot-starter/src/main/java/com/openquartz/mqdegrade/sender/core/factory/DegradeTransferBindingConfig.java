package com.openquartz.mqdegrade.sender.core.factory;

import com.openquartz.mqdegrade.sender.common.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 绑定降级传输配置.(手动方式绑定降级配置)
 *
 * <p>使用样例：
 * <blockquote><pre>
 *      DegradeTransferBindingConfig
 *                 .builder("test2")
 *                 // 直接发送
 *                 .send(String.class, messageProducer::sendMessage2)
 *                 // 第一个消费分组降级传输去
 *                 .degrade("test2_group1", String.class, msg -> {
 *                     degradeMessageManager.degradeTransfer2(msg);
 *                     return true;
 *                 })
 *                 // 第二个消费分组降级传输
 *                 .degrade("test2_group2", String.class, msg -> {
 *                     degradeMessageManager.degradeTransfer3(msg);
 *                     return true;
 *                 })
 *                 .binding();
 * </pre></blockquote>
 * </p>
 *
 * @author svnee
 */
public class DegradeTransferBindingConfig {

    private DegradeTransferBindingConfig() {
    }

    @SuppressWarnings("all")
    public static class DegradeTransferBindingTransferConfigBuilder {

        private final String resource;

        private final Pair<Class, Predicate> sendPair;

        private DegradeTransferBindingTransferConfigBuilder(String resource, Pair<Class, Predicate> sendPair) {
            this.resource = resource;
            this.sendPair = sendPair;
        }

        private Map<String, Pair<Class, Predicate>> degradeList = new ConcurrentHashMap<>();

        /**
         * 降级传输配置
         *
         * @param messageClazz     降级传输类
         * @param degradePredicate 降级传输函数
         * @param <T>              T 降级传输消息类
         * @return builder
         */
        public <T> DegradeTransferBindingTransferConfigBuilder degrade(String degradeResource, Class<T> messageClazz, Predicate<T> degradePredicate) {
            assert messageClazz != null;
            assert degradePredicate != null;
            this.degradeList.putIfAbsent(degradeResource, Pair.of(messageClazz, degradePredicate));
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

            degradeList.forEach((degradeResource, v) -> {
                DegradeRouterFactory.register(resource, degradeResource, v.getKey(), v.getValue());
            });
        }
    }

    @SuppressWarnings("all")
    public static class DegradeTransferBindingConfigBuilder {

        private final String resource;

        private Map<String, Pair<Class, Predicate>> degradeList = new ConcurrentHashMap<>();

        private DegradeTransferBindingConfigBuilder(String resource) {
            this.resource = resource;
        }

        /**
         * 直接发送配置
         *
         * @param messageClazz  消息类型
         * @param sendPredicate 发送函数
         * @param <T>           T 消息类
         * @return builder
         */
        public <T> DegradeTransferBindingTransferConfigBuilder send(Class<T> messageClazz, Predicate<T> sendPredicate) {
            assert messageClazz != null;
            assert sendPredicate != null;
            Pair<Class, Predicate> sendPair = Pair.of(messageClazz, sendPredicate);
            return new DegradeTransferBindingTransferConfigBuilder(resource, sendPair);
        }
    }

    public static DegradeTransferBindingConfigBuilder builder(String resource) {
        return new DegradeTransferBindingConfigBuilder(resource);
    }
}
