/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aizuda.easy.retry.server.support.allocate.server;

import com.aizuda.easy.retry.server.support.allocate.common.ConsistentHashRouter;
import com.aizuda.easy.retry.server.support.allocate.common.HashFunction;
import com.aizuda.easy.retry.server.support.ServerLoadBalance;
import com.aizuda.easy.retry.server.support.allocate.common.Node;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Consistent Hashing queue algorithm
 */
public class AllocateMessageQueueConsistentHash implements ServerLoadBalance {

    private final int virtualNodeCnt;
    private final HashFunction customHashFunction;

    public AllocateMessageQueueConsistentHash() {
        this(10);
    }

    public AllocateMessageQueueConsistentHash(int virtualNodeCnt) {
        this(virtualNodeCnt, null);
    }

    public AllocateMessageQueueConsistentHash(int virtualNodeCnt, HashFunction customHashFunction) {
        if (virtualNodeCnt < 0) {
            throw new IllegalArgumentException("illegal virtualNodeCnt :" + virtualNodeCnt);
        }
        this.virtualNodeCnt = virtualNodeCnt;
        this.customHashFunction = customHashFunction;
    }

    @Override
    public List<String> allocate(String currentCID, List<String> groupList,
                                      List<String> serverList) {

        if (currentCID == null || currentCID.length() < 1) {
            throw new IllegalArgumentException("currentCID is empty");
        }
        if (CollectionUtils.isEmpty(groupList)) {
            throw new IllegalArgumentException("mqAll is null or mqAll empty");
        }
        if (CollectionUtils.isEmpty(serverList)) {
            throw new IllegalArgumentException("cidAll is null or cidAll empty");
        }

        List<String> result = new ArrayList<>();
        if (!serverList.contains(currentCID)) {
            return result;
        }

        Collection<ClientNode> cidNodes = new ArrayList<ClientNode>();
        for (String cid : serverList) {
            cidNodes.add(new ClientNode(cid));
        }

        final ConsistentHashRouter<ClientNode> router; //for building hash ring
        if (customHashFunction != null) {
            router = new ConsistentHashRouter<ClientNode>(cidNodes, virtualNodeCnt, customHashFunction);
        } else {
            router = new ConsistentHashRouter<ClientNode>(cidNodes, virtualNodeCnt);
        }

        List<String> results = new ArrayList<>();
        for (String groupName : groupList) {
            ClientNode clientNode = router.routeNode(groupName);
            if (clientNode != null && currentCID.equals(clientNode.getKey())) {
                results.add(groupName);
            }
        }

        return results;

    }

    @Override
    public String getName() {
        return "CONSISTENT_HASH";
    }

    private static class ClientNode implements Node {
        private final String clientID;

        public ClientNode(String clientID) {
            this.clientID = clientID;
        }

        @Override
        public String getKey() {
            return clientID;
        }
    }

}
