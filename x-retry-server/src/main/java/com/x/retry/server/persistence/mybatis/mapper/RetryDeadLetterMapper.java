package com.x.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.x.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.web.model.request.RetryDeadLetterQueryVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RetryDeadLetterMapper extends BaseMapper<RetryDeadLetter> {

    int insertBatch(@Param("retryDeadLetters") List<RetryDeadLetter> retryDeadLetters,  @Param("partition") Integer partition);

    int countRetryDeadLetterByCreateAt(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("partition") Integer partition);

    List<RetryDeadLetter> searchAllByPage(PageDTO<RetryTask> pageDTO,@Param("queryVO") RetryDeadLetterQueryVO queryVO);
}
