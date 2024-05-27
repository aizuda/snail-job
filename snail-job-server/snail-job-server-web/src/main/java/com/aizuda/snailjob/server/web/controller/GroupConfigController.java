package com.aizuda.snailjob.server.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.GroupConfigResponseVO;
import com.aizuda.snailjob.server.web.service.GroupConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
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
 * 重试组接口
 *
 * @author: opensnail
 * @date : 2021-11-22 14:38
 */
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupConfigController {
    private static final List<String> FILE_EXTENSIONS = List.of("json");
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
    public Boolean updateGroupStatus(@RequestBody @Validated GroupConfigRequestVO groupConfigRequestVO) {
        String groupName = groupConfigRequestVO.getGroupName();
        Integer groupStatus = groupConfigRequestVO.getGroupStatus();
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
    @LoginRequired
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new SnailJobCommonException("Please select a file to upload");
        }

        // 保存文件到服务器
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!FILE_EXTENSIONS.contains(suffix)) {
            throw new SnailJobCommonException("文件类型错误");
        }

        JsonNode node = JsonUtil.toJson(file.getBytes());

        List<GroupConfigRequestVO> requestList = JsonUtil.parseList(JsonUtil.toJsonString(node),
            GroupConfigRequestVO.class);

        // 校验参数是否合法
        for (final GroupConfigRequestVO groupConfigRequestVO : requestList) {
            ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
            Validator validator = vf.getValidator();
            Set<ConstraintViolation<GroupConfigRequestVO>> set = validator.validate(groupConfigRequestVO);
            for (final ConstraintViolation<GroupConfigRequestVO> violation : set) {
                throw new SnailJobCommonException(violation.getMessage());
            }
        }

        groupConfigService.importGroup(requestList);
    }

    @PostMapping("/export")
    @LoginRequired
    @OriginalControllerReturnValue
    public ResponseEntity<String> exportGroup(@RequestBody Set<Long> groupIds) {
        Assert.notEmpty(groupIds, () -> new SnailJobServerException("参数错误"));

        String configs = groupConfigService.exportGroup(groupIds);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 设置下载时的文件名称
        String fileName = String.format("group-config-%s.json", DateUtils.toNowFormat(DateUtils.PURE_DATETIME_MS_PATTERN));
        String disposition = "attachment; filename=" +
                             new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, disposition);
        return ResponseEntity.ok()
            .headers(headers)
            .body(configs);
    }
}
