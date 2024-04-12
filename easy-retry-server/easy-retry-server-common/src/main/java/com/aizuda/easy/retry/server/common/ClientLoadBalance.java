package com.aizuda.easy.retry.server.common;

import java.util.TreeSet;

/**
 * @author: opensnail
 * @date : 2022-03-11 21:18
 */
public interface ClientLoadBalance {

    String route(String key, TreeSet<String> clientAllAddressSet);

    int routeType();

}
