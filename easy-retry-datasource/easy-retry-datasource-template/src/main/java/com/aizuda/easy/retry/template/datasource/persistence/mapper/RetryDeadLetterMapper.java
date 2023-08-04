package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RetryDeadLetterMapper extends BaseMapper<RetryDeadLetter> {

    int insertBatch(@Param("retryDeadLetters") List<RetryDeadLetter> retryDeadLetters,  @Param("partition") Integer partition);

    int countRetryDeadLetterByCreateAt(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("partition") Integer partition);

//    List<RetryDeadLetter> searchAllByPage(PageDTO<RetryTask> pageDTO,@Param("queryVO") RetryDeadLetterQueryVO queryVO);
}
