package com.x.retry.server.web.controller;

import com.x.retry.server.service.SceneConfigService;
import com.x.retry.server.web.annotation.LoginRequired;
import com.x.retry.server.web.model.base.PageResult;
import com.x.retry.server.web.model.request.SceneConfigQueryVO;
import com.x.retry.server.web.model.response.SceneConfigResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
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
