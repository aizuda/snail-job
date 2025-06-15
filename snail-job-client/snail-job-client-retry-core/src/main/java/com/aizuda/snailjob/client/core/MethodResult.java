package com.aizuda.snailjob.client.core;

public interface MethodResult {

    class NoMethodResult implements MethodResult {
        @Override
        public Object result(String scene, String executorClassName, Object[] args, Throwable throwable) {
            return null;
        }
    }

    /**
     *
     * @param scene 重试场景值
     * @param executorClassName 重试类名称
     * @param args 重试方法参数
     * @param throwable 重试异常
     * @return 重试方法返回值
     */
    Object result(String scene, String executorClassName, Object[] args, Throwable throwable);
}
