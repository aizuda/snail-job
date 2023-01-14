package com.example.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 学生
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-24
 */
@Data
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer age;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
