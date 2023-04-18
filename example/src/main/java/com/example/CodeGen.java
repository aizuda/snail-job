package com.example;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * @author: www.byteblogs.com
 * @date : 2021-09-30 17:19
 */
public class CodeGen {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/demo", "root", "root")
                .globalConfig(builder -> {
                    builder.author("www.byteblogs.com") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir("/Users/zhangshuguang/easy-retry/example/src/main/java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.example") // 设置父包名
                            .moduleName("") // 设置父包模块名
                            .entity("po")
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "/Users/zhangshuguang/easy-retry/example/src/main/resources/mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("school", "student", "teacher", "school_student_teacher") // 设置需要生成的表名
                            .entityBuilder().idType(IdType.AUTO).formatFileName("%s")
                            .serviceBuilder().formatServiceFileName("%sService")
                            .mapperBuilder().enableBaseResultMap().enableMapperAnnotation()
                    ; // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}
