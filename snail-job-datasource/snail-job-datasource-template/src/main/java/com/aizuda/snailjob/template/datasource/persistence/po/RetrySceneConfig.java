package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 场景配置
 *
 * @author zuojunlin
 * @date 2023-11-19 22:05:25
 * @since 2.5.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sj_retry_scene_config")
public class RetrySceneConfig extends CreateUpdateDt {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private Integer blockStrategy;

    private Integer sceneStatus;

    private Integer maxRetryCount;

    private Integer backOff;

    private String triggerInterval;

    private String description;

    private Long deadlineRequest;

    private Integer routeKey;

    private Integer executorTimeout;

    /**
     * 通知告警场景配置id列表
     */
    private String notifyIds;

    /**
     * 回调状态 0、不开启 1、开启
     */
    private Integer cbStatus;

    /**
     * 回调触发类型
     */
    private Integer cbTriggerType;

    /**
     * 回调的最大执行次数
     */
    private int cbMaxCount;

    /**
     * 回调间隔时间
     */
    private String cbTriggerInterval;

    /**
     * 标签
     */
    private String labels;

}
