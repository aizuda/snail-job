package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportGroupVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.snailjob.server.web.model.request.GroupStatusUpdateRequestVO;
import com.aizuda.snailjob.server.web.model.response.GroupConfigResponseVO;
import com.aizuda.snailjob.server.web.service.GroupConfigService;
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

/**
 * 重试组接口
 *
 * @author: opensnail
 * @date : 2021-11-22 14:38
 */
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupConfigController {
    private final GroupConfigService groupConfigService;

    @LoginRequired(role = RoleEnum.ADMIN)
    @PostMapping("")
    public Boolean addGroup(@RequestBody @Validated GroupConfigRequestVO groupConfigRequestVO) {
        return groupConfigService.addGroup(groupConfigRequestVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @PutMapping("")
    public Boolean updateGroup(@RequestBody @Validated GroupConfigRequestVO groupConfigRequestVO) {
        return groupConfigService.updateGroup(groupConfigRequestVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @PutMapping("status")
    public Boolean updateGroupStatus(@RequestBody @Validated GroupStatusUpdateRequestVO requestVO) {
        String groupName = requestVO.getGroupName();
        Integer groupStatus = requestVO.getGroupStatus();
        return groupConfigService.updateGroupStatus(groupName, groupStatus);
    }

    @LoginRequired
    @GetMapping("list")
    public PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO) {
        return groupConfigService.getGroupConfigForPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{groupName}")
    public GroupConfigResponseVO getGroupConfigByGroupName(@PathVariable("groupName") String groupName) {
        return groupConfigService.getGroupConfigByGroupName(groupName);
    }

    @LoginRequired
    @PostMapping("/all/group-config/list")
    public List<GroupConfigResponseVO> getAllGroupNameList(@RequestBody List<String> namespaceIds) {
        return groupConfigService.getAllGroupConfigList(namespaceIds);
    }

    @LoginRequired
    @GetMapping("/all/group-name/list")
    public List<String> getAllGroupNameList() {
        return groupConfigService.getAllGroupNameList();
    }

    @LoginRequired
    @GetMapping("/on-line/pods/{groupName}")
    public List<String> getOnlinePods(@PathVariable("groupName") String groupName) {
        return groupConfigService.getOnlinePods(groupName);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @GetMapping("/partition-table/list")
    public List<Integer> getTablePartitionList() {
        return groupConfigService.getTablePartitionList();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LoginRequired(role = RoleEnum.ADMIN)
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        groupConfigService.importGroup(ImportUtils.parseList(file, GroupConfigRequestVO.class));
    }

    @PostMapping("/export")
    @LoginRequired
    @OriginalControllerReturnValue
    public ResponseEntity<String> exportGroup(@RequestBody ExportGroupVO exportGroupVO) {
        return ExportUtils.doExport(groupConfigService.exportGroup(exportGroupVO));
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @DeleteMapping("{groupName}")
    public boolean deleteByGroupName(@PathVariable("groupName") String groupName) {
        return groupConfigService.deleteByGroupName(groupName);
    }
}
