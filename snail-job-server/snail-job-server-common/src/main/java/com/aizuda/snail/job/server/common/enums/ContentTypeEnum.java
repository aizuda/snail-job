package com.aizuda.snail.job.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

/**
 * @author: xiaowoniu
 * @date : 2024-01-03
 * @since : 2.6.0
 */
@AllArgsConstructor
@Getter
public enum ContentTypeEnum {

    JSON(1, MediaType.APPLICATION_JSON),
    FORM(2, MediaType.APPLICATION_FORM_URLENCODED)
    ;

    private final Integer type;
    private final MediaType mediaType;

    public static ContentTypeEnum valueOf(Integer type) {
        for (ContentTypeEnum contentTypeEnum : values()) {
            if (contentTypeEnum.getType().equals(type)) {
                return contentTypeEnum;
            }
        }

        return ContentTypeEnum.JSON;
    }
}
