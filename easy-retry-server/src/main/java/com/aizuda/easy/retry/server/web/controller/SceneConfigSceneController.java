package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.service.SceneConfigService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.response.SceneConfigResponseVO;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 重试场景接口
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:27
 */
@RestController
@RequestMapping("/scene-config")
public class SceneConfigSceneController {

    @Autowired
    private SceneConfigService sceneConfigService;

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
}
