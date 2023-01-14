package com.example;

import com.example.mapper.SchoolMapper;
import com.example.po.School;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class ExampleApplicationTests {

    @Autowired
    private SchoolMapper schoolMapper;

    @Test
    public void test() {
        School school = new School();
        school.setAddress("上海");
        school.setName("复旦");
        schoolMapper.insert(school);
    }


}
