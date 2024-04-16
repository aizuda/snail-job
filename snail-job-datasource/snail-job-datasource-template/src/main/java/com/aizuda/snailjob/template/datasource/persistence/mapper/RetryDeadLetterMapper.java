package com.aizuda.snailjob.template.datasource.persistence.mapper;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RetryDeadLetterMapper extends BaseMapper<RetryDeadLetter> {

    int insertBatch(@Param("retryDeadLetters") List<RetryDeadLetter> retryDeadLetter);

}
