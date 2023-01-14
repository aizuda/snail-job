package com.x.retry.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.retry.common.core.enums.NodeTypeEnum;
import com.x.retry.common.core.enums.RetryStatusEnum;
import com.x.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.service.DashBoardService;
import com.x.retry.server.web.model.response.ActivePodQuantityResponseVO;
import com.x.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.x.retry.server.web.model.response.SceneQuantityRankResponseVO;
import com.x.retry.server.web.model.response.TaskQuantityResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:19
 */
@Service
public class DashBoardServiceImpl implements DashBoardService {

    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Autowired
    private ServerNodeMapper serverNodeMapper;

    @Override
    public TaskQuantityResponseVO countTask() {

        TaskQuantityResponseVO taskQuantityResponseVO = new TaskQuantityResponseVO();
        taskQuantityResponseVO.setTotal(retryTaskLogMapper.countTaskTotal());

        taskQuantityResponseVO.setFinish(retryTaskLogMapper.countTaskByRetryStatus(RetryStatusEnum.FINISH.getLevel()));
        taskQuantityResponseVO.setMaxRetryCount(retryTaskLogMapper.countTaskByRetryStatus(RetryStatusEnum.MAX_RETRY_COUNT.getLevel()));
        taskQuantityResponseVO.setRunning(taskQuantityResponseVO.getTotal() - taskQuantityResponseVO.getFinish() - taskQuantityResponseVO.getMaxRetryCount());

        return taskQuantityResponseVO;
    }

    @Override
    public DispatchQuantityResponseVO countDispatch() {
        DispatchQuantityResponseVO dispatchQuantityResponseVO = new DispatchQuantityResponseVO();

        Long total = retryTaskLogMapper.selectCount(null);
        dispatchQuantityResponseVO.setTotal(total);

        if (total == 0) {
            return dispatchQuantityResponseVO;
        }

        Long success = retryTaskLogMapper.selectCount(new LambdaQueryWrapper<RetryTaskLog>()
                .in(RetryTaskLog::getRetryStatus, RetryStatusEnum.MAX_RETRY_COUNT.getLevel(),
                        RetryStatusEnum.FINISH.getLevel()));
        dispatchQuantityResponseVO.setSuccessPercent(BigDecimal.valueOf(success).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));

        return dispatchQuantityResponseVO;
    }

    @Override
    public ActivePodQuantityResponseVO countActivePod() {

        ActivePodQuantityResponseVO activePodQuantityResponseVO = new ActivePodQuantityResponseVO();
        activePodQuantityResponseVO.setTotal(serverNodeMapper.selectCount(null));
        activePodQuantityResponseVO.setServerTotal(serverNodeMapper.selectCount(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType())));
        activePodQuantityResponseVO.setClientTotal(serverNodeMapper.selectCount(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getNodeType, NodeTypeEnum.CLIENT.getType())));

        return activePodQuantityResponseVO;
    }

    @Override
    public List<SceneQuantityRankResponseVO> rankSceneQuantity(String groupName) {
        return retryTaskLogMapper.rankSceneQuantity(groupName);
    }

    @Override
    public List<DispatchQuantityResponseVO> lineDispatchQuantity(String groupName) {

        List<DispatchQuantityResponseVO> totalDispatchQuantityResponseList = retryTaskLogMapper.lineDispatchQuantity(groupName, null);

        List<DispatchQuantityResponseVO> successDispatchQuantityResponseList = retryTaskLogMapper.lineDispatchQuantity(groupName, RetryStatusEnum.FINISH.getLevel());
        Map<String, DispatchQuantityResponseVO> dispatchQuantityResponseVOMap = successDispatchQuantityResponseList.stream().collect(Collectors.toMap(DispatchQuantityResponseVO::getCreateDt, i -> i));
        for (DispatchQuantityResponseVO dispatchQuantityResponseVO : totalDispatchQuantityResponseList) {

            DispatchQuantityResponseVO quantityResponseVO = dispatchQuantityResponseVOMap.get(dispatchQuantityResponseVO.getCreateDt());
            if (Objects.isNull(dispatchQuantityResponseVO)) {
                dispatchQuantityResponseVO.setSuccess(0L);
            } else {
                dispatchQuantityResponseVO.setSuccess(quantityResponseVO.getTotal());
            }

            dispatchQuantityResponseVO.setFail(dispatchQuantityResponseVO.getTotal() - dispatchQuantityResponseVO.getSuccess());

        }

        return totalDispatchQuantityResponseList;
    }
}
