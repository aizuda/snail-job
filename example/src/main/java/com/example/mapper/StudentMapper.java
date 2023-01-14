package com.example.mapper;

import com.example.po.Student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生 Mapper 接口
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-24
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
