package com.example;

import com.example.demo.TestRetryMethodComplexParamaterService;
import com.example.model.Cat;
import com.example.model.Dog;
import com.example.model.Zoo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-21 11:32
 */
@SpringBootTest
public class RetryMethodComplexParamaterServiceTest {

    @Autowired
    private TestRetryMethodComplexParamaterService testRetryMethodComplexParamaterService;

    @SneakyThrows
    @Test
    public void testRetryMethodForZoo() {
        Zoo zoo = new Zoo();

        Dog dog = new Dog();
        dog.setName(UUID.randomUUID().toString());
        dog.setAge(10);
        zoo.setDog(dog);

        Cat cat = new Cat();
        cat.setAge(1);
        cat.setName(UUID.randomUUID().toString());
        zoo.setList(Arrays.asList(cat));

        try {
            testRetryMethodComplexParamaterService.testRetryMethod(zoo);
        } catch (Exception e) {
        }

        Thread.sleep(90000);
    }

    @SneakyThrows
    @Test
    public void testRetryMethodForZooList() {
        Zoo zoo = new Zoo();

        Dog dog = new Dog();
        dog.setName(UUID.randomUUID().toString());
        dog.setAge(10);
        zoo.setDog(dog);

        Cat cat = new Cat();
        cat.setAge(1);
        cat.setName(UUID.randomUUID().toString());
        zoo.setList(Arrays.asList(cat));

        try {
            testRetryMethodComplexParamaterService.testRetryMethod(Arrays.asList(zoo), dog);
        } catch (Exception e) {
        }

        Thread.sleep(90000);
    }

}
