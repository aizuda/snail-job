package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.handler.GetHttpRequestHandler;
import com.aizuda.snailjob.server.job.task.support.convert.JobExecutorConverter;
import com.aizuda.snailjob.server.job.task.support.handler.DistributedLockHandler;
import com.aizuda.snailjob.server.model.dto.JobExecutorDTO;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;


/**
 * 接收客户端执行器注册请求处理
 *
 * @since 1.6.0
 */
@Component
@AllArgsConstructor
public class RegisterJobExecutorsHttpRequestHandler extends GetHttpRequestHandler {
    // 分布式锁
    private DistributedLockHandler distributedLockHandler;

    private static final String KEY = "register_{0}_{1}";

    private final AccessTemplate accessTemplate;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.REGISTER_JOB_EXECUTORS.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Beat register job executors content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object arg = retryRequest.getArgs()[0];
        if (arg == null) {
            return new SnailJobRpcResult(StatusEnum.NO.getStatus(), "Beat register job executors arg is null", Boolean.FALSE, retryRequest.getReqId());
        }
        String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());
        String namespace = headers.get(HeadersEnum.NAMESPACE.getKey());
        String lockName = processLockName(MessageFormat.format(KEY, groupName, namespace));
        distributedLockHandler.lockWithDisposableAndRetry(() -> {

            List<JobExecutorDTO> executors = JsonUtil.parseList(JsonUtil.toJsonString(arg), JobExecutorDTO.class);
            LambdaQueryWrapper<JobExecutors> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(JobExecutors::getGroupName, groupName)
                    .eq(JobExecutors::getNamespaceId, namespace);
            List<JobExecutors> dbExecutors = accessTemplate.getJobExecutorAccess().list(queryWrapper);
            List<String> dbExecutorsList = dbExecutors.stream().map(JobExecutors::getJobExecutorsName).toList();
            List<JobExecutorDTO> toAddExecutors = executors.stream().filter(e -> !dbExecutorsList.contains(e.getJobExecutorsName())).toList();
            if (toAddExecutors.isEmpty()) {
                SnailJobLog.LOCAL.warn("Beat register job executors toAddExecutors is empty");
                return;
            }
            List<JobExecutors> jobExecutors = JobExecutorConverter.INSTANCE.toJobExecutors(toAddExecutors);
            accessTemplate.getJobExecutorAccess().insertBatch(jobExecutors);


        }, lockName, Duration.ofSeconds(2), Duration.ofSeconds(2), 3);
        return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "Batch Register Job Executors Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
    }

    /**
     * 处理分布式锁名称
     * @param input     锁名称
     * @return          处理后的锁名称
     */
    public static String processLockName(String input) {
        if (input.length() <= 64) {
            return input;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            // 返回完整的 64 位 SHA-256 哈希值
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
