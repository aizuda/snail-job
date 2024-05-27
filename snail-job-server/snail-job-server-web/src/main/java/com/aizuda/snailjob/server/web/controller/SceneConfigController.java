package com.aizuda.snailjob.server.web.controller;

import cn.hutool.core.io.FileUtil;
import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snailjob.server.web.service.SceneConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * 重试场景接口
 *
 * @author: opensnail
 * @date : 2022-03-03 11:27
 */
@RestController
@RequestMapping("/scene-config")
@RequiredArgsConstructor
public class SceneConfigController {

    private static final List<String> FILE_EXTENSIONS = List.of("json");
    private final SceneConfigService sceneConfigService;

    @LoginRequired
    @GetMapping("page/list")
    public PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO queryVO) {
        return sceneConfigService.getSceneConfigPageList(queryVO);
    }

    @LoginRequired
    @GetMapping("list")
    public List<SceneConfigResponseVO> getSceneConfigList(@RequestParam("groupName") String groupName) {
        return sceneConfigService.getSceneConfigList(groupName);
    }

    @LoginRequired
    @GetMapping("{id}")
    public SceneConfigResponseVO getSceneConfigDetail(@PathVariable("id") Long id) {
        return sceneConfigService.getSceneConfigDetail(id);
    }

    @LoginRequired
    @PutMapping("/{id}/status/{status}")
    public Boolean updateStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        return sceneConfigService.updateStatus(id, status);
    }

    @LoginRequired
    @PostMapping
    public Boolean saveSceneConfig(@RequestBody @Validated SceneConfigRequestVO requestVO) {
        return sceneConfigService.saveSceneConfig(requestVO);
    }

    @LoginRequired
    @PutMapping
    public Boolean updateSceneConfig(@RequestBody @Validated SceneConfigRequestVO requestVO) {
        return sceneConfigService.updateSceneConfig(requestVO);
    }

    @PostMapping("/import")
    @LoginRequired
    public void importScene(final MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new SnailJobCommonException("Please select a file to upload");
        }

        // 保存文件到服务器
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!FILE_EXTENSIONS.contains(suffix)) {
            throw new SnailJobCommonException("文件类型错误");
        }

        JsonNode node = JsonUtil.toJson(file.getBytes());
        List<SceneConfigRequestVO> requestList = JsonUtil.parseList(JsonUtil.toJsonString(node),
            SceneConfigRequestVO.class);

        // 校验参数是否合法
        for (final SceneConfigRequestVO sceneConfigRequestVO : requestList) {
            ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
            Validator validator = vf.getValidator();
            Set<ConstraintViolation<SceneConfigRequestVO>> set = validator.validate(sceneConfigRequestVO);
            for (final ConstraintViolation<SceneConfigRequestVO> violation : set) {
                throw new SnailJobCommonException(violation.getMessage());
            }
        }

        // 写入数据
        sceneConfigService.importSceneConfig(requestList);
    }

    @LoginRequired
    @PostMapping("/export")
    @OriginalControllerReturnValue
    public ResponseEntity<String> export(@RequestBody Set<Long> sceneIds) {
        String configs = sceneConfigService.exportSceneConfig(sceneIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 设置下载时的文件名称
        String fileName = String.format("retry-scene-%s.json", DateUtils.toNowFormat(DateUtils.PURE_DATETIME_MS_PATTERN));
        String disposition = "attachment; filename=" +
        new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, disposition);
        return ResponseEntity.ok()
            .headers(headers)
            .body(configs);
    }

}
