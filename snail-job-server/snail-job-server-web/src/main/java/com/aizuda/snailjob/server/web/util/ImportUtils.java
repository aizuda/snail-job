package com.aizuda.snailjob.server.web.util;

import cn.hutool.core.io.FileUtil;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public final class ImportUtils {

    private static final List<String> FILE_EXTENSIONS = List.of("json");
    private ImportUtils() {}

    public static @NotNull <VO> List<VO> parseList(MultipartFile file, Class<VO> clazz) throws IOException {
        if (file.isEmpty()) {
            throw new SnailJobCommonException("请选择一个文件上传");
        }

        // 保存文件到服务器
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!FILE_EXTENSIONS.contains(suffix)) {
            throw new SnailJobCommonException("文件类型错误");
        }

        JsonNode node = JsonUtil.toJson(file.getBytes());
        return JsonUtil.parseList(JsonUtil.toJsonString(node), clazz);
    }
}


