package com.aizuda.easy.retry.common.core.log;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wodeyangzipingpingwuqi
 * @version 2.6.0
 * @date 2023/12/27
 */
@Data
public class TaskLogFieldDTO implements Serializable {

    private String name;
    private String value;

    /**
     * Non arg constructor for Serializable.
     */
    @SuppressWarnings("unused")
    public TaskLogFieldDTO() {

    }

    public TaskLogFieldDTO(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
