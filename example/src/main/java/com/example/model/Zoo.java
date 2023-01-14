package com.example.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-21 11:17
 */
@Data
public class Zoo {
    private Dog dog;
    private List<Cat> list;
    private LocalDateTime now;
}
