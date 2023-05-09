package com.aizuda.easy.retry.server.support.generator;

/**
 * 分布式Id生成器
 *
 * @author www.byteblogs.com
 * @date 2023-05-04
 * @since 2.0
 */
public interface IdGenerator {

    /**
     * 获取匹配的模式
     *
     * @param mode 1. 雪花算法(默认算法) 2.号段模式
     * @return
     */
    boolean supports(int mode);

    /**
     * 获取分布式id
     *
     * @param group 组
     * @return id
     */
    String idGenerator(String group);

}
