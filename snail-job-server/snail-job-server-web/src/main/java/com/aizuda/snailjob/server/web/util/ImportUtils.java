package com.aizuda.snailjob.server.web.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ImportUtils {

    private static final List<String> FILE_EXTENSIONS = List.of("json");

    public static @NotNull <VO> List<VO> parseList(MultipartFile file, Class<VO> clazz) throws IOException {
        // 保存文件到服务器
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!FILE_EXTENSIONS.contains(suffix)) {
            throw new SnailJobCommonException("文件类型错误");
        }

        JsonNode node = JsonUtil.toJson(file.getBytes());
        List<VO> requestList = JsonUtil.parseList(JsonUtil.toJsonString(node), clazz);
        Assert.notEmpty(requestList, () -> new SnailJobServerException("导入数据不能为空"));

        // 校验参数是否合法
        requestList.forEach(vo -> {
            ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
            Validator validator = vf.getValidator();
            Set<ConstraintViolation<VO>> rules = validator.validate(vo);
            rules.forEach(rule -> {
                throw new SnailJobCommonException(rule.getMessage());
            });
        });

        return requestList;
    }
}


