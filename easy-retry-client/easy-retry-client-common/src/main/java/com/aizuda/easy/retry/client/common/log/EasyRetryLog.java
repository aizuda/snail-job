package com.aizuda.easy.retry.client.common.log;

/**
 * 静态日志类，用于在不引入日志对象的情况下打印日志
 *
 * @author wodeyangzipingpingwuqi
 */
public final class EasyRetryLog {

    public static Local LOCAL = new Local();

    public static Remote REMOTE = new Remote();
}
