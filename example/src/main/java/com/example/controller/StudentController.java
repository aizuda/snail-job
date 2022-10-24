package com.example.controller;


import com.example.demo.TestExistsTransactionalRetryService;
import com.x.retry.common.core.annotation.OriginalControllerReturnValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * <p>
 * 学生 前端控制器
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-24
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private TestExistsTransactionalRetryService testExistsTransactionalRetryService;

    @GetMapping("query")
    @OriginalControllerReturnValue
    public String get() {
       return testExistsTransactionalRetryService.testSimpleInsert(UUID.randomUUID().toString());
    }
}
