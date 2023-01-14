package com.x.retry.server.persistence.support.access.retry;

import com.x.retry.server.config.RequestDataHelper;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.support.RetryTaskAccess;
import org.springframework.core.Ordered;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-24 14:27
 */
public abstract class AbstractRetryTaskAccess implements RetryTaskAccess<RetryTask>, Ordered {

   public abstract boolean support(String groupId);

   /**
    * 设置分区
    *
    * @param groupName 组名称
    */
   public void setPartition(String groupName) {
      RequestDataHelper.setPartition(groupName);
   }
}
