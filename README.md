

<p align="center">
  <a href="https://gitee.com/aizuda/easy-retry">
   <img alt="Easy-Retry-Logo" src="doc/images/logo.png">
  </a>
</p>

<p align="center">
     分布式重试服务平台 Easy-Retry
</p>


# 简介
>在分布式系统大行其道的当前，系统数据的准确性和正确性是重大的挑战，基于CAP理论，采用柔性事务，保障系统可用性以及数据的最终一致性成为技术共识
>为了保障分布式服务的可用性，服务容错性，服务数据一致性 以及服务间掉用的网络问题。依据"墨菲定律"，增加核心流程重试，数据核对校验成为提高系统鲁棒性常用的技术方案
>
> 通常的业务场景有： 
> + 保障系统稳定性，减少网络抖动导致异常，增加重试能力
>+ 保障服务容错性，对核心流程进行拆分，在业务低峰期进行数据核对
>+ 保证信息的可达性，在服务间通知时增加重试
> 
> 但由于正常业务场景较难触发重试流程，从而导致研发测试对重试场景和流量并不重视，始终处于重要但无序的"管理真空"
>
>Easy-RETRY 是一个针对业务系统重试流量的治理平台，其自身具有高可用高性能高负载的特点，服务特性有：
> + 支持千万级别的重试流量分派
> + 支持流量容量扩容，自动识别并处理
> + 支持流量处理节点水平扩容
> + 高效利用系统资源支持高并发
> + 支持多种算法调度客户端执行
> + 打包上报，支持高并发业务场景
> + 加密通讯，保障信息安全

# 流量管理平台预览
地址: <http://preview.easyretry.com/>
账号: admin
密码: admin

## 特别用户
<a href="http://aizuda.com/?from=mp">![aizuda.png](doc/images/aizuda.png)</a>

## 相关链接
- [字节跳动: 如何优雅地重试](https://juejin.cn/post/6914091859463634951)
- [文档](https://www.easyretry.com/pages/a2f161/)
- [功能实例](https://www.easyretry.com/pages/960e25/)
## 原理
- [客户端原理剖析](https://gitee.com/aizuda/easy-retry/tree/dev/example)
- [服务端原理剖析](https://gitee.com/aizuda/easy-retry/tree/dev/example)

## 应用实例
- [Spring-Boot](https://gitee.com/aizuda/easy-retry/tree/dev/example)

## 期望
欢迎提出更好的意见，帮助完善 Easy-Retry

## 版权
[Apache-2.0](https://gitee.com/aizuda/easy-retry/blob/master/LICENSE)
