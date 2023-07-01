package com.aizuda.easy.retry.server.persistence.mybatis.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ServerNode implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupName;

    private String hostId;

    private String hostIp;

    private Integer hostPort;

    private LocalDateTime expireAt;

    private Integer nodeType;

    private String contextPath;

    private String extAttrs;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    private static final long serialVersionUID = 1L;

}
