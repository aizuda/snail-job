package com.aizuda.easy.retry.server.support;

import java.util.TreeSet;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-11 21:18
 */
public interface ClientLoadBalance {

    String route(String currentGroupName, TreeSet<String> clientAllAddressSet);

    int routeType();

}
