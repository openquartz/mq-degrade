# MQ Degradation Strategy

[中文版本](README.md)|**English**

## Welcome to Star!!!

**[Home](https://openquartz.github.io/)** | **[GitHub](https://github.com/openquartz/mq-degrade)**

MQ (Message Queue) is increasingly being used in microservices systems, even appearing in some core business processes.
However, the stability of MQ must be well guaranteed.
From a developer's perspective, what can be done regarding MQ service stability is to minimize the impact on services when MQ encounters issues, especially on core business flows.
Common MQ stability assurance strategies include: multi-cluster deployment, degradation transmission, etc.

This document outlines a strategy specifically designed for degradation transmission. It mainly focuses on MQ stability assurance from the development side. \
There are two common implementation approaches for degradation transmission:**Push Mode** and **Pull Mode**.

- I. Push Mode (based on MQ producers)

In push mode, the MQ producer pushes messages to the MQ, which then pushes them to consumers. When the MQ service becomes abnormal, the producer degrades the transmission to other channels to deliver messages to consumers. \
For example, when MQ is abnormal, the producer automatically sends messages through HTTP/RPC interfaces to notify consumers.

**Advantages**: Simple transformation, less prone to traffic spikes, relatively high stability. Automatic degradation triggering can be configured (custom degradation rules), requiring no manual intervention. \
**Disadvantages**: MQ consumer QPS is limited by storage QPS; QPS limits may be exceeded during high traffic.

- II. Pull Mode (based on MQ consumers)

In pull mode, consumers actively pull messages from the MQ. When the MQ service becomes abnormal, consumers pull data directly from the producer's interface into their own service.

The commonly used middleware implementation for **Push Mode** is `mq-degrade-send-spring-boot-starter`. Using this middleware enables rapid implementation of MQ degradation transmission with minimal intrusion.  
**Push Mode Degradation** Integration Guide: [MQ Degradation Transmission - Push Mode (Integration Guide)](./mq-degrade-send-spring-boot-starter/README.md)