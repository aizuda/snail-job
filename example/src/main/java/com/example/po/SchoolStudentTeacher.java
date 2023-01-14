package com.example.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 学校学生老师表
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-24
 */
@TableName("school_student_teacher")
@Data
public class SchoolStudentTeacher implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private Long teacherId;

    private Long studentId;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
