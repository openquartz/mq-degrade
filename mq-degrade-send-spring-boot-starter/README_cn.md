## MQ Degradation Transmission - Push Mode (Integration Guide)

> **Note**: This project has not been published to the Maven Central Repository and needs to be manually added to a local or private repository for use.

[中文版本](README.md) | [English](README_cn.md)

### Quick Start

#### 1. Add Dependencies

- For Maven projects:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>mq-degrade-send-spring-boot-starter</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

- For Gradle projects:

`implementation 'com.openquartz:mq-degrade-send-spring-boot-starter:${lastVersion}'`

#### 2. Execute SQL Script

```sql
CREATE TABLE `mq_degrade_message_entity`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key ID',
    `resource`        varchar(255) NOT NULL COMMENT 'Resource Name',
    `message`         text COMMENT 'Message Content',
    `msg_key`         varchar(255) DEFAULT NULL COMMENT 'Message Key',
    `context`         text COMMENT 'Context Information',
    `ip_addr`         varchar(64)  DEFAULT NULL COMMENT 'IP Address',
    `create_time`     datetime     NOT NULL COMMENT 'Creation Time',
    `retry_count`     int(11) DEFAULT '0' COMMENT 'Retry Count',
    `last_retry_time` datetime     DEFAULT NULL COMMENT 'Last Retry Time',
    PRIMARY KEY (`id`),
    KEY               `idx_resource` (`resource`),
    KEY               `idx_msg_key` (`msg_key`),
    KEY               `idx_ip_retry_last_retry` (`ip_addr`, `retry_count`, `last_retry_time`),
    KEY               `idx_retry_last_retry` (`retry_count`, `last_retry_time`),
    KEY               `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ Degradation Message Entity Table';
```

Adjust the size and type of the [message](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/persist/model/DegradeMessageEntity.java#L22-L22) and [context](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/persist/model/DegradeMessageEntity.java#L32-L32) fields based on your service's message sizes as needed.

#### 3. Configuration Code

##### 3.1 Configure JdbcTemplate

```java
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Degradation Message JdbcTemplate
 * @param dataSource Data Source
 * @return JdbcTemplate
 */
@Bean(name = "degradeMessageJdbcTemplate")
public JdbcTemplate degradeMessageJdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

##### 3.2 Binding Send and Degradation Routers

###### 3.2.1 Manual Binding

```java
DegradeTransferBindingConfig
        .builder("test2")
        // Direct send method
        .send(String.class, messageProducer::sendMessage2)
        // Degradation transfer for first consumer group
        .degrade("test2_group1", String.class, msg -> {
            degradeMessageManager.degradeTransfer2(msg);
            return true;
        })
        // Degradation transfer for second consumer group
        .degrade("test2_group2", String.class, msg -> {
            degradeMessageManager.degradeTransfer3(msg);
            return true;
        }).binding();
```

##### 3.2.2 Annotation-Based Binding
###### 3.2.2.1 Send Method
```java
@SendRouter(resource = "SendTest")
public boolean send(String msg) {
    // TODO Send message
    return true;
}
```
###### 3.2.2.2 Degradation Method
```java
import com.openquartz.mqdegrade.sender.annotation.DegradeRouter;

@DegradeRouter(resource = "SendTest", degradeResource = "test2_group1")
public boolean send(String msg) {
    // TODO Send message
    return true;
}
```

##### 3.4 Sending MQ Messages

###### 3.4.1 Manual Sending

```java
import com.openquartz.mqdegrade.sender.core.send.SendMessageFacade;

import javax.annotation.Resource;

@Resource
private SendMessageFacade sendMessageFacade;

public boolean sendMessage(String msg) {
    return sendMessageFacade.send(msg, "SendTest");
}
```

###### 3.4.2 Annotation-based Sending (Recommended) (Minimal Impact to Existing Process)

```java
import com.openquartz.mqdegrade.sender.annotation.SendRouter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
class Test1 {

    @SendRouter(resource = "SendTest")
    public boolean send(String msg) {
        // TODO Send message
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

#### 4. Configuration

All configuration settings have the prefix `mq.degrade`.

- Common Settings

| Configuration                                                  | Description                     | Default Value | Remarks |
|----------------------------------------------------------------|----------------------------------|---------------|---------|
| mq.degrade.common.enable                                       | Enable MQ degradation           | true          |         |
| mq.degrade.common.enable-force-degrade                         | Enable global force degradation | false         |         |
| mq.degrade.common.enable-auto-degrade                          | Enable global auto degradation  | false         |         |
| mq.degrade.common.resource-degrade.{resource}.enable-force-degrade       | Enable force degradation per resource   | false |             |
| mq.degrade.common.resource-degrade.{resource}.enable-auto-degrade        | Enable auto degradation per resource    | false |             |
| mq.degrade.common.resource-degrade.{resource}.auto-degrade-sentinel-resource | Custom Sentinel degradation resource per resource |       |             |
| mq.degrade.common.resource-degrade.{resource}.enable-parallel-degrade-transfer | Enable parallel degradation transfer per resource | false |             |

- Configuration Settings

| Configuration                              | Description                | Default Value | Remarks                                  |
|-------------------------------------------|-----------------------------|---------------|------------------------------------------|
| mq.degrade.config.namespace               | Default configuration space | application   | Apollo config center default is application. |

- Transmission Settings

| Configuration                                              | Description                  | Default Value                    | Remarks |
|-----------------------------------------------------------|-------------------------------|----------------------------------|---------|
| mq.degrade.transfer.enable                                | Enable parallel transmission  | false                            |         |
| mq.degrade.transfer.thread-pool.thread-prefix             | Thread pool prefix for parallel transmission | mq-parallel-transfer-thread |         |
| mq.degrade.transfer.thread-pool.core-pool-size            | Core thread count in thread pool |                                 |         |
| mq.degrade.transfer.thread-pool.keep-alive-time          | Keep alive time for threads in thread pool |                             |         |
| mq.degrade.transfer.thread-pool.max-pool-size             | Max thread count in thread pool |                                  |         |
| mq.degrade.transfer.thread-pool.queue-capacity            | Queue capacity in thread pool |                                    |         |

- Compensation Settings

| Configuration                                                 | Description                      | Default Value | Remarks                   |
|--------------------------------------------------------------|-----------------------------------|---------------|---------------------------|
| mq.degrade.compensate.enable                                 | Enable degradation compensation | true          | Default enabled            |
| mq.degrade.compensate.alert-enable                           | Enable alert for degradation compensation | true | Default enabled             |
| mq.degrade.compensate.max-retry-count                        | Max retry count for compensation | 15            |                           |
| mq.degrade.compensate.limit-count                            | Number of records processed per compensation run | 10 |                           |
| mq.degrade.compensate.global.backoff-interval-time          | Global backoff interval time for compensation | 3600 seconds |                     |
| mq.degrade.compensate.global.delay-time                     | Global delay time for compensation | 600 seconds |                               |
| mq.degrade.compensate.global.period-time                     | Global period time for compensation | 600 seconds |                               |
| mq.degrade.compensate.self.backoff-interval-time            | Local backoff interval time for compensation | 600 seconds |                       |
| mq.degrade.compensate.self.delay-time                       | Local delay time for compensation | 0 seconds |                                   |
| mq.degrade.compensate.self.period-time                      | Local period time for compensation | 600 seconds |                                |
| mq.degrade.compensate.alert.backoff-interval-time           | Alert backoff interval time for compensation | 7200 seconds |                      |
| mq.degrade.compensate.alert.delay-time                      | Alert delay time for compensation | 1800 seconds |                                 |
| mq.degrade.compensate.alert.period-time                     | Alert period time for compensation | 600 seconds |                                 |

- Filtering Settings

| Configuration                                      | Description                     | Default Value | Remarks                                     |
|---------------------------------------------------|----------------------------------|---------------|---------------------------------------------|
| mq.degrade.filter.resource-filter.{resource}      | Enable filtering at resource level | false        | If enabled, degraded messages will not be stored for automatic compensation. |

#### 5. Startup

### Extension Point Support

#### 1. Custom Alert Message Sending Support

The service provides an interface [com.openquartz.mqdegrade.sender.common.alert.DegradeAlert](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/common/alert/DegradeAlert.java#L6-L13) for degradation alerts. The default implementation logs to local files; users can customize implementations like WeChat, DingTalk, Email, etc., by injecting them into Spring.

#### 2. Auto-refresh Support for Apollo Config

Degradation compensation configurations in this starter generally use `org.springframework.cloud.context.config.annotation.RefreshScope` for auto-refreshing. It also natively supports `Apollo Config`. If you want to use other configuration centers like Nacos, integrate accordingly using `RefreshScope`.

#### 3. Custom Compensation Scheduler Support

Compensation defaults to using local thread pools without relying on third-party schedulers. Users may connect to third-party schedulers such as xxl-job, dis-job, powerjob, etc., by calling the appropriate methods in [com.openquartz.mqdegrade.sender.core.compensate.DegradeMessageCompensateService](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/core/compensate/DegradeMessageCompensateService.java#L9-L41).

#### 4. Interceptor Support

Interceptors support both sending and degradation interception. When multiple interceptors are used, they execute in priority order. You can use interceptors for **Metrics monitoring**, **custom alerts**, and **interruptions**.

- **Send Interception**
  Implement interface: [com.openquartz.mqdegrade.sender.core.interceptor.SendInterceptor](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/core/interceptor/SendInterceptor.java#L9-L34)

- **Degradation Interception**
  Implement interface: [com.openquartz.mqdegrade.sender.core.interceptor.DegradeTransferInterceptor](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/core/interceptor/DegradeTransferInterceptor.java#L9-L35)

Custom interceptors can be injected into the factory [com.openquartz.mqdegrade.sender.core.interceptor.InterceptorFactory](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/core/interceptor/InterceptorFactory.java#L9-L74).

#### 5. Auto-Degradation Support

To enable auto-degradation, implement the interface [com.openquartz.mqdegrade.sender.core.degrade.AutoDegradeSupport](file:///Users/jackxu/Documents/Code/github.com/openquartz/mq-degrade/mq-degrade-send-spring-boot-starter/src/main/java/com/openquartz/mqdegrade/sender/core/degrade/AutoDegradeSupport.java#L8-L19). 

The SDK already implements this interface with [Sentinel](https://sentinelguard.io/). To use it, add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>${sentinel.version}</version>
</dependency>
```

This enables auto-degradation based on Sentinel.