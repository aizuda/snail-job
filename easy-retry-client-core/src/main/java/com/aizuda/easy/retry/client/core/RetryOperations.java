package com.aizuda.easy.retry.client.core;

/**
 * 手动生成重试任务模板类
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-09 16:32
 */
public interface RetryOperations {

   /**
    * 生成重试任务并上报服务端，且存在嵌套重试情况下不上报服务端
    *
    */
   void generateAsyncTask();

   /**
    * 生成重试任务并上报服务端
    *
    * @param forceReport true: 表示存在嵌套重试情况下强制上报服务端 false: 表示存在嵌套重试情况下不上报服务端
    */
   void generateAsyncTask(boolean forceReport);

   /**
    * 生成重试任务并上报服务端，且存在嵌套重试情况下不上报服务端
    *
    */
   Boolean generateSyncTask();

   /**
    * 生成重试任务并上报服务端
    *
    * @param forceReport true: 表示存在嵌套重试情况下强制上报服务端 false: 表示存在嵌套重试情况下不上报服务端
    */
   Boolean generateSyncTask(boolean forceReport);

}
