package com.aizuda.easy.retry.client.core;

/**
 * 业务id生成器
 * 同一个组的同一个场景下只会存在一个相同的bizId重试任务, 若存在相同的则上报服务后会被幂等处理
 * 比如:
 * 组: AGroup
 * 场景: BScene
 * 时刻1: 上报一个异常 bizId: A1 状态为重试中
 * 时刻2: 上报一个异常 bizId: A2 状态为重试中，可以上报成功，此时存在两个重试任务
 * 时刻3: 上报一个异常 bizId: A1 不会新增一个重试任务，会被幂等处理
 * 时刻4:  bizId: A1 重试完成, 状态为已完成
 * 时刻5: 上报一个异常 bizId: A1 状态为重试中, 新增一条重试任务
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-08 09:42
 */
public interface BizIdGenerate {

    /**
     * 参数列表为Object一维数组, 下面说明每一个下标代表的数据含义
     * 0: 场景名称: scene(String)
     * 1: 执行器名称: targetClassName(String)
     * 2: 参数列表: args(Object[])
     * 3: 执行的方法名称: methodName(String)
     * scene, targetClassName, args, executorMethod.getName()
     *
     * @param t 参数列表
     * @return bizId
     * @throws Exception
     */
    String idGenerate(Object... t) throws Exception;
}