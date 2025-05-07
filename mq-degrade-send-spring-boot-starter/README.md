## MQ降级传输-推模式（接入指南）

### 快速开始

#### 1、引入依赖

- maven项目引入

```xml

<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>mq-degrade-send-spring-boot-starter</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

- gradle项目引入

`implementation 'com.openquartz:mq-degrade-send-spring-boot-starter:${lastVersion}'`

#### 2、执行SQL脚本

```sql
CREATE TABLE `mq_degrade_message_entity`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `resource`        varchar(255) NOT NULL COMMENT '资源名称',
    `message`         text COMMENT '发送消息内容',
    `msg_key`         varchar(255) DEFAULT NULL COMMENT '消息key',
    `context`         text COMMENT '上下文信息',
    `ip_addr`         varchar(64)  DEFAULT NULL COMMENT 'IP地址',
    `create_time`     datetime     NOT NULL COMMENT '创建时间',
    `retry_count`     int(11) DEFAULT '0' COMMENT '重试次数',
    `last_retry_time` datetime     DEFAULT NULL COMMENT '最新重试时间',
    PRIMARY KEY (`id`),
    KEY               `idx_resource` (`resource`),
    KEY               `idx_msg_key` (`msg_key`),
    KEY               `idx_ip_retry_last_retry` (`ip_addr`, `retry_count`, `last_retry_time`),
    KEY               `idx_retry_last_retry` (`retry_count`, `last_retry_time`),
    KEY               `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ降级消息实体表';
```

根据自身服务的发送消息的大小，来决定message和context字段的大小和类型。可做适当的调整。

#### 3、配置代码

#### 3.1 配置JdbcTemplate

```java
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 降级消息JdbcTemplate
 * @param dataSource 数据源
 * @return JdbcTemplate
 */
@Bean(name = "degradeMessageJdbcTemplate")
public JdbcTemplate degradeMessageJdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

#### 3.2 绑定发送消息的路由

##### 3.2.1 手动方式绑定

```java

import com.openquartz.mqdegrade.sender.core.factory.SendRouterFactory;

String resource = "SendTest";
SendRouterFactory.register(resource, String .class, req ->{
        // TODO 发送消息
        return true;
});

```

#### 3.2.2 注解方式绑定

```java

@SendRouter(resource = "SendTest")
public boolean send(String msg) {
    // TODO 发送消息
    return true;
}

```

#### 3.3 配置MQ降级传输

##### 3.3.1 手动方式绑定

```java
import com.openquartz.mqdegrade.sender.core.factory.DegradeRouterFactory;

String resource = "SendTest";
DegradeRouterFactory.register(resource, String .class, req ->{
        // TODO 发送消息
        return true;
});
```

##### 3.3.2 注解方式绑定

```java
import com.openquartz.mqdegrade.sender.annotation.DegradeRouter;

@DegradeRouter(resource = "SendTest")
public boolean send(String msg) {
    // TODO 发送消息
    return true;
}
```

##### 3.4 MQ消息发送

##### 3.4.1 手动方式发送

```java
import com.openquartz.mqdegrade.sender.core.send.SendMessageFacade;

import javax.annotation.Resource;

@Resource
private SendMessageFacade sendMessageFacade;

public boolean sendMessage(String msg) {
    return sendMessageFacade.send(msg, "SendTest");
}

```

##### 3.4.1 注解方式发送

```java
import com.openquartz.mqdegrade.sender.annotation.SendRouter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
class Test1 {

    @SendRouter(resource = "SendTest")
    public boolean send(String msg) {
        // TODO 发送消息
        return true;
    }
}

class Test2 {

    @Resource
    private Test1 test1;

    public boolean sendMessage(String msg) {
        return test1.send(msg);
    }
}
```

#### 4、配置

所有配置的设置统一前缀`mq.degrade`

- 公共相关

| 配置                                                                             | 描述                    | 默认值   | 备注 |
|--------------------------------------------------------------------------------|-----------------------|-------|----|
| mq.degrade.common.enable                                                       | 是否开启MQ-降级             | true  |    |
| mq.degrade.common.enable-force-degrade                                         | 是否开启MQ-全局强制降级         | false |    |
| mq.degrade.common.enable-auto-degrade                                          | 是否开启MQ-全局自动降级         | false |    |
| mq.degrade.common.resource-degrade.{resource}.enable-force-degrade             | 是否开启MQ-resource强制降级   | false |    |
| mq.degrade.common.resource-degrade.{resource}.enable-auto-degrade              | 是否开启MQ-resource自动降级   | false |    |
| mq.degrade.common.resource-degrade.{resource}.auto-degrade-sentinel-resource   | 自定义 资源对应的Sentinel降级资源 |       |    |
| mq.degrade.common.resource-degrade.{resource}.enable-parallel-degrade-transfer | 是否开启MQ-resource并行降级传输 | false |    |

- 配置相关

| 配置                          | 描述     | 默认值         | 备注                              |
|-----------------------------|--------|-------------|---------------------------------|
| mq.degrade.config.namespace | 默认配置空间 | application | apollo 配置中心配置空间，默认取application. |

- 传输相关

| 配置                                              | 描述            | 默认值                         | 备注 |
|-------------------------------------------------|---------------|-----------------------------|----|
| mq.degrade.transfer.enable                      | 是否开启并行传输      | false                       |    |
| mq.degrade.transfer.thread-pool.thread-prefix   | 并行传输线程池前缀     | mq-parallel-transfer-thread |    |
| mq.degrade.transfer.thread-pool.core-pool-size  | 并行传输线程池核心线程数  |                             |    |
| mq.degrade.transfer.thread-pool.keep-alive-time | 并行传输线程池保持活跃时间 |                             |    |
| mq.degrade.transfer.thread-pool.max-pool-size   | 并行传输线程池最大线程数  |                             |    |
| mq.degrade.transfer.thread-pool.queue-capacity  | 并行传输线程池队列容量   |                             |    |

- 补偿相关

| 配置                                                 | 描述             | 默认值  | 备注            |
|----------------------------------------------------|----------------|------|---------------|
| mq.degrade.compensate.enable                       | 是否开启降级补偿       | true | 默认开启自带的降级补偿   |
| mq.degrade.compensate.alert-enable                 | 是否开启降级补偿预警     | true | 默认开启自带的降级补偿预警 |
| mq.degrade.compensate.max-retry-count              | 开启降级补偿最大重试次数   | 15   |               |
| mq.degrade.compensate.limit-count                  | 降级补偿一次处理条数     | 10   |               |
| mq.degrade.compensate.global.backoff-interval-time | 降级补偿-全局-回溯间隔时间 | 3600 | 单位：秒          |
| mq.degrade.compensate.global.delay-time            | 降级补偿-全局-延时补偿时间 | 600  | 单位：秒          |
| mq.degrade.compensate.global.period-time           | 降级补偿-全局-延时周期时间 | 600  | 单位：秒          |
| mq.degrade.compensate.self.backoff-interval-time   | 降级补偿-本机-回溯间隔时间 | 600  | 单位：秒          |
| mq.degrade.compensate.self.delay-time              | 降级补偿-本机-延时补偿时间 | 0    | 单位：秒          |
| mq.degrade.compensate.self.period-time             | 降级补偿-本机-延时周期时间 | 600  | 单位：秒          |
| mq.degrade.compensate.alert.backoff-interval-time  | 降级补偿-预警-回溯间隔时间 | 7200 | 单位：秒          |
| mq.degrade.compensate.alert.delay-time             | 降级补偿-预警-延时补偿时间 | 1800 | 单位：秒          |
| mq.degrade.compensate.alert.period-time            | 降级补偿-预警-延时周期时间 | 600  | 单位：秒          |

- 过滤相关

| 配置                                           | 描述            | 默认值   | 备注                         |
|----------------------------------------------|---------------|-------|----------------------------|
| mq.degrade.filter.resource-filter.{resource} | 是否开启资源级别过滤不存储 | false | 开启过滤后降级传输时将不存储到自动补偿到源MQ队列中 |

#### 5、启动

### 扩展点支持

#### 1、自定义预警消息发送支持

服务提供降级预警接口 `com.openquartz.mqdegrade.sender.common.alert.DegradeAlert`
.默认实现采用日志打印到本地，用户可以自定义实现微信/钉钉/邮件等。将预警Bean注入到Spring中。

#### 2、Apollo Config配置自动刷新支持

starter中的降级补偿配置基本使用了`org.springframework.cloud.context.config.annotation.RefreshScope`的自动刷新方式。并默认兼容了
`Apollo Config`配置自动刷新。
如果用想使用其他的配置中心的刷新服务，例如：nacos等。可以自行接入并使用`RefreshScrope`刷新配置。

#### 3、自定义补偿调度支持

服务补偿默认使用do-Raper的方式进行调度，使用本机线程池。不依赖第三方调度中心。如果用户有需要接入自身使用的第三方调度中心。例如xxl-job,dis-job,powerjob
等。可以自行实现。
只需调用`com.openquartz.mqdegrade.sender.core.compensate.DegradeMessageCompensateService` 类中的对应的补偿接口即可。

#### 4、拦截器支持

拦截器支持发送拦截和降级拦截支持。多个拦截器时按照优先级顺序执行。
用户可以使用拦截器做**Metrics打点监控**、**自定义预警**、**中断**等操作。

- 发送拦截
  可以实现接口: `com.openquartz.mqdegrade.sender.core.interceptor.SendInterceptor`

- 降级拦截
  可以实现接口: `com.openquartz.mqdegrade.sender.core.interceptor.DegradeTransferInterceptor`

用户可以将自定义的拦截器实现注入到`com.openquartz.mqdegrade.sender.core.interceptor.InterceptorFactory` 工厂中即可。

#### 5、自动降级支持

如果开启自动降级配置，需要用户提供自动降级的实现对应的接口
`com.openquartz.mqdegrade.sender.core.degrade.AutoDegradeSupport`.

sdk 默认实现了基于Sentinel的`com.openquartz.mqdegrade.sender.core.degrade.AutoDegradeSupport`接口，
需要用户引入sentinel 相关依赖

```xml

<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>${sentinel.version}</version>
</dependency>

```

即可开启使用基于Sentinel的自动降级。