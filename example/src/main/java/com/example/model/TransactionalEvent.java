package com.example.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author: shuguang.zhang
 * @date : 2023-04-25 22:48
 */
@Getter
public class TransactionalEvent<T> extends ApplicationEvent {

    private T data;

    public TransactionalEvent(final T source) {
        super(source);
    }
}
