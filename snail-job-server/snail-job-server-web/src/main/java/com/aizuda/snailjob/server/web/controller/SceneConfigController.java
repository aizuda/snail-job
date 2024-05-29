package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.snailjob.server.web.service.SceneConfigService;
import com.aizuda.snailjob.server.web.util.ExportUtils;
import com.aizuda.snailjob.server.web.util.ImportUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @LoginRequired
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new SnailJobCommonException("请选择一个文件上传");
        }

        // 写入数据
        sceneConfigService.importSceneConfig(ImportUtils.parseList(file, SceneConfigRequestVO.class));
    }

    @LoginRequired
    @PostMapping("/export")
    @OriginalControllerReturnValue
    public ResponseEntity<String> export(@RequestBody Set<Long> sceneIds) {
        return ExportUtils.doExport(sceneConfigService.exportSceneConfig(sceneIds));

    }

}
