package com.aizuda.snailjob.common.core.model;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author opensnail
 * @date 2022-03-08
 * @since 2.0
 */
@Data
public class SnailJobRequest {

   private static final AtomicLong REQUEST_ID = new AtomicLong(0);

   private long reqId;

   private Object[] args;

   public SnailJobRequest(Object... args) {
      this.args = args;
      this.reqId = newId();
   }

   private static long newId() {
      return REQUEST_ID.getAndIncrement();
   }

   public SnailJobRequest() {
   }

   @Override
   public String toString() {
      return JsonUtil.toJsonString(this);
   }
}
