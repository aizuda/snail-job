

<p align="center">
  <a href="https://www.easyretry.com">
   <img alt="Easy-Retry-Logo" src="doc/images/logo.png" width="350px">
  </a>
</p>

<p align="center">
     致力提高分布式业务系统一致性的分布式重试平台
</p>


# 简介
> 在当前广泛流行的分布式系统中，确保系统数据的一致性和正确性是一项重大挑战。为了解决分布式事务问题，涌现了许多理论和业务实践，其中BASE理论是目前业界广泛接受的分布式一致性理论。
> 基于BASE理论，采用柔性事务并优先保障系统的可用性和数据的最终一致性已逐渐成为技术共识。
> 为了确保分布式服务的可用性和数据一致性，并防止由于网络抖动、连接超时等问题导致短时不可用的情况，根据"墨菲定律"，在核心流程中增加重试和数据核对校验的动作成为提高系统鲁棒性常用的技术方案。
> 在此背景下EasyRetry应运而生。EasyRetry是一款基于BASE思想实现的分布式服务重试组件，旨在通过重试机制确保数据的最终一致性。它提供了控制台任务观测、可配置的重试策略、重试后执行回调以及丰富地告警配置等功能。通过这些手段，可以对异常数据进行全面监测和回放，从而在确保系统高可用性的同时，大大提升数据的一致性。

> 通常的业务场景有： 
>+ 保障系统稳定性，减少网络抖动导致异常，增加重试能力
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
- [这款分布式重试组件,治好了我的重试强迫症！](https://juejin.cn/post/7249607108043145274)
- [系统简介](https://www.easyretry.com/pages/d1d1da/)
- [架构与功能](https://www.easyretry.com/pages/540554/)

## 原理
- [场景应用](https://www.easyretry.com/pages/406a68/)
- [HelloWorld](https://www.easyretry.com/pages/da9ecc/)

## 应用实例
- [easy-retry-demo](https://gitee.com/zhangyutongxue/easy-retry-demo)

## 期望
欢迎提出更好的意见，帮助完善 Easy-Retry

## 版权
[Apache-2.0](https://gitee.com/aizuda/easy-retry/blob/master/LICENSE)
