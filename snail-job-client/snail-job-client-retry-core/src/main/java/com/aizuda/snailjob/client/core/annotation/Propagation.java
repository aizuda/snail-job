/*
 * Copyright (c) 2024 .
 *
 * SnailJob - 灵活，可靠和快速的分布式任务重试和分布式任务调度平台
 * > ✅️ 可重放，可管控、为提高分布式业务系统一致性的分布式任务重试平台
 * > ✅️ 支持秒级、可中断、可编排的高性能分布式任务调度平台
 *
 * Aizuda/SnailJob 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点:
 *
 * 1. Aizuda/SnailJob已申请知识产权保护,二次开发如用于开源竞品请先联系群主沟通，禁止任何变相的二开行为，未经审核视为侵权;
 * 2. 不得修改产品相关代码的源码头注释和出处;
 * 3. 不得进行简单修改包装声称是自己的产品;
 * 4. 不得应用于危害国家安全、荣誉和利益的行为，不能以任何形式用于非法目的;
 *
 */
package com.aizuda.snailjob.client.core.annotation;

/**
 * Snail Job 重试任务传播机制
 *
 * @author: xiaowoniu
 * @date : 2024-02-05
 * @since : 3.1.0
 */
public enum Propagation {

    /**
     * 当设置为REQUIRED时，如果当前重试存在，就加入到当前重试中，即外部入口触发重试
     * 如果当前重试不存在，就创建一个新的重试任务。
     */
    REQUIRED,

    /**
     * 当设置为REQUIRES_NEW时，
     * 无论当前重试任务是否存在，都会一个新的重试任务。
     */
    REQUIRES_NEW
    ;

}
