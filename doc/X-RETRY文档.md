## 《分布式异常重试服务平台 X-RETRY》


# 简介
X-RETRY 基于服务治理的思想我们开发了重试治理的功能，支持动态配置，接入方式基本无需入侵业务代码，并使用多种策略结合的方式在链路层面控制重试放大效应，兼顾易用性、灵活性、安全性的分布式异常重试服务平台

# 社区
https://www.byteblogs.com/chat

# 特性
1. 管控重试流量，预防重试风暴，及早发现和预警，并且提供流程管理手段
2. 保证易用性: 业务接入成本小。避免依赖研发人员的技术水平，保障重试的稳定性
3. 灵活性: 能够动态调整配置,启动/停止任务,以及终止运行中的重试数据
4. 操作简单:一分钟上手，支持WEB页面对重试数据CRUD操作。
5. 数据大盘: 实时管控系统重试数据。
6. 多样化退避策略: Cron、固定间隔、等级触发、随机时间触发
7. 容器化部署: 服务端支持docker容器部署
8. 高性能调度平台: 支持服务端节点动态扩容和缩容
9. 多样化重试类型: 支持ONLY_LOCAL、ONLY_REMOTE、LOCAL_REMOTE多种重试类型
10. 重试数据管理: 可以做到重试数据不丢失、重试数据一键回放
11. 支持多样化的告警方式: 邮箱、企业微信、钉钉

# 快速入门
## 添加依赖
```java
<dependency>
    <groupId>com.x.retry</groupId>
    <artifactId>x-retry-client-starter</artifactId>
    <version>0.0.0.1-SNAPSHOT</version>
</dependency>
```

## 配置
添加注解开启X-RETRY功能
```java
@SpringBootApplication
@EnableXRetry(group = "example_group")
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

}
```

为需要重试的方法添加重试注解
```java
@Retryable(scene = "errorMethodForLocalAndRemote", localTimes = 3, retryStrategy = RetryType.LOCAL_REMOTE)
    public String errorMethodForLocalAndRemote(String name) {

        double i = 1 / 0;

        return "这是一个简单的异常方法";
    }
```
## Retryable 详解
|属性|类型|必须指定|默认值|描述|
|-|-|-|-|-|
| scene |String|是|无|场景|
| include | Throwable |否|无|包含的异常|
| exclude |Throwable|否|无|排除的异常|
| retryStrategy|RetryType|是|LOCAL_REMOTE|重试策略|
| retryMethod|RetryMethod|是|RetryAnnotationMethod|重试处理入口|
| bizId | BizIdGenerate |是| SimpleBizIdGenerate |自定义业务id，默认为hash(param),传入成员列表，全部拼接取hash|
| bizNo |String|否|无| bizNo spel表达式|
| localTimes |int|是|3| 本地重试次数 次数必须大于等于1|
| localInterval |int|是|2| 本地重试间隔时间(s)|

## 初始化数据库
数据库脚本位置
```
doc/sql/x_retry.sql
```


