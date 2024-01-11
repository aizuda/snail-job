package com.aizuda.easy.retry.common.core.log;

import com.aizuda.easy.retry.common.core.constant.LogFieldConstant;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wodeyangzipingpingwuqi
 * @date 2023-12-27
 * @since 2.6.0
 */
@Data
public class LogContentDTO {

    private List<TaskLogFieldDTO> fieldList;

    public LogContentDTO() {
        this.fieldList = new ArrayList<>();
    }

    public Map<String, String> toMap() {
        return fieldList
            .stream()
            .filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
            .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue));
    }

    public void addField(String name, String value) {
        fieldList.add(new TaskLogFieldDTO(name, value));
    }

    public void addTimeField(String time) {
        this.addField(LogFieldConstant.TIME, time);
    }

    public void addTimeStamp(Long timeStamp) {
        this.addField(LogFieldConstant.TIME_STAMP, String.valueOf(timeStamp));
    }

    public Long getTimeStamp() {
        return Long.parseLong(fieldList.stream().filter(taskLogFieldDTO -> !Objects.isNull(taskLogFieldDTO.getValue()))
            .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue))
            .get(LogFieldConstant.TIME_STAMP));
    }

    public void addLevelField(String level) {
        this.addField(LogFieldConstant.LEVEL, level);
    }

    public void addThreadField(String thread) {
        this.addField(LogFieldConstant.THREAD, thread);
    }

    public void addMessageField(String message) {
        this.addField(LogFieldConstant.MESSAGE, message);
    }

    public void addLocationField(String location) {
        this.addField(LogFieldConstant.LOCATION, location);
    }

    public void addThrowableField(String throwable) {
        this.addField(LogFieldConstant.THROWABLE, throwable);
    }

}
