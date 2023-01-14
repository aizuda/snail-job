package com.x.retry.server.persistence.mybatis.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotifyConfig implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupName;

    private Integer notifyType;

    private String notifyAddress;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private String description;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    private static final long serialVersionUID = 1L;


}