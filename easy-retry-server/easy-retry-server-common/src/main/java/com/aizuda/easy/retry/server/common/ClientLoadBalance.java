package com.aizuda.easy.retry.server.common;

import java.util.TreeSet;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-11 21:18
 */
public interface ClientLoadBalance {

    String route(String key, TreeSet<String> clientAllAddressSet);

    int routeType();

}
