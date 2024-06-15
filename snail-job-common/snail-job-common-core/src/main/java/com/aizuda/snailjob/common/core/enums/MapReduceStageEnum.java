package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2024-06-12
 * @since : sj_1.1.0
 */
@Getter
@AllArgsConstructor
public enum MapReduceStageEnum {
    MAP(1),
    REDUCE(2),
    MERGE_REDUCE(3);

    private final int stage;

    public static MapReduceStageEnum ofStage(Integer stage) {
        if (Objects.isNull(stage)) {
            return null;
        }

        for (MapReduceStageEnum value : MapReduceStageEnum.values()) {
            if (value.getStage() == stage) {
                return value;
            }
        }

        return null;
    }
}
