package com.aizuda.easy.retry.server.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wodeyangzipingpingwuqi
 * @version 1.0.0
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
