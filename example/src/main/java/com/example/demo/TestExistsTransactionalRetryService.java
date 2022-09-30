package com.example.demo;

import com.example.mapper.SchoolMapper;
import com.example.mapper.StudentMapper;
import com.example.po.School;
import com.example.po.Student;
import com.x.retry.client.core.annotation.Retryable;
import com.x.retry.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-26 09:08
 */
@Component
public class TestExistsTransactionalRetryService {

    @Autowired
    private SchoolMapper schoolMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private RemoteService remoteService;

    @Retryable(scene = "testSimpleInsert", bizNo = "#name", localTimes = 3, isThrowException = false)
    @Transactional
    public String testSimpleInsert(String name) {

        School school = new School();
        school.setName(name);
        school.setAddress(UUID.randomUUID().toString());
        school.setCreateDt(LocalDateTime.now());
        school.setUpdateDt(LocalDateTime.now());
        schoolMapper.insert(school);

        Student student = new Student();
        student.setName(name);
        student.setAge(1);
        student.setCreateDt(LocalDateTime.now());
        student.setUpdateDt(LocalDateTime.now());
        studentMapper.insert(student);

        Result call = remoteService.call();
        System.out.println("-------------->"+call.getMessage());
        if (call.getStatus() == 0) {
            throw new UnsupportedOperationException("调用远程失败" + school.getAddress());
        }

        return "testSimpleInsert"+school.getAddress();
    }

}
