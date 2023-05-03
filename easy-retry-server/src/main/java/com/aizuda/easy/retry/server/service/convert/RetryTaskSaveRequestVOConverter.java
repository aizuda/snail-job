package com.aizuda.easy.retry.server.service.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: shuguang.zhang
 * @date : 2023-04-26 15:13
 */
@Mapper
public class RetryTaskSaveRequestVOConverter {

   public static final RetryTaskSaveRequestVOConverter INSTANCE = Mappers.getMapper(RetryTaskSaveRequestVOConverter.class);

}
