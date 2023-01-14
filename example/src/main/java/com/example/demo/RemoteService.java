package com.example.demo;

import com.x.retry.common.core.model.Result;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-26 10:14
 */
@Component
public class RemoteService {

    public Result call() {
       return new Result();
    }
}
