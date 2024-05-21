package com.aizuda.snailjob.server.common.register;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.Register;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author opensnail
 * @date 2023-06-07
 * @since 1.6.0
 */
@Slf4j
public abstract class AbstractRegister implements Register, Lifecycle {

    @Autowired
    protected ServerNodeMapper serverNodeMapper;

    @Override
    public boolean register(RegisterContext context) {

        beforeProcessor(context);

        ServerNode serverNode = initServerNode(context);

        boolean result = doRegister(context, serverNode);

        afterProcessor(serverNode);

        return result;
    }

    protected abstract void afterProcessor(final ServerNode serverNode);

    protected void refreshExpireAt(List<ServerNode> serverNodes) {

        for (final ServerNode serverNode : serverNodes) {
            serverNode.setExpireAt(getExpireAt());
        }

        // 批量更新
        if (serverNodes.size() != serverNodeMapper.updateBatchExpireAt(serverNodes)) {
            try {
                serverNodeMapper.insertBatch(serverNodes);
            } catch (DuplicateKeyException ignored) {
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("注册节点失败", e);
            }
        }

        for (final ServerNode serverNode : serverNodes) {
            // 刷新本地缓存过期时间
            CacheRegisterTable.refreshExpireAt(serverNode);
        }

    }

    protected abstract void beforeProcessor(RegisterContext context);

    protected ServerNode initServerNode(RegisterContext context) {

        ServerNode serverNode = new ServerNode();
        serverNode.setHostId(context.getHostId());
        serverNode.setHostIp(context.getHostIp());
        serverNode.setNamespaceId(context.getNamespaceId());
        serverNode.setGroupName(context.getGroupName());
        serverNode.setHostPort(context.getHostPort());
        serverNode.setNodeType(getNodeType());
        serverNode.setCreateDt(LocalDateTime.now());
        serverNode.setExtAttrs(context.getExtAttrs());

        return serverNode;
    }

    protected abstract LocalDateTime getExpireAt();


    protected abstract boolean doRegister(RegisterContext context, ServerNode serverNode);


    protected abstract Integer getNodeType();


}
