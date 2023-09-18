package com.aizuda.easy.retry.server.common.handler;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * www.byteblogs.com
 *
 * @author: shuguang.zhang
 * @date : 2023-09-21 09:26
 */
public class DistributeInstance {

    private CopyOnWriteArraySet<Integer> CONSUMER_BUCKET = new CopyOnWriteArraySet<>();

}
