package com.aizuda.snail.job.common.log.dto;

import java.io.Serializable;

/**
 * @author wodeyangzipingpingwuqi
 * @version 2.6.0
 * @date 2023/12/27
 */
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

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
