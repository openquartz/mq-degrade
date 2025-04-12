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

public boolean sendMessage(String msg){
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

TODO

#### 5、启动

### 扩展点支持

#### 1、自定义预警消息发送支持

#### 2、Apollo Config配置自动刷新支持

#### 3、自定义补偿调度支持