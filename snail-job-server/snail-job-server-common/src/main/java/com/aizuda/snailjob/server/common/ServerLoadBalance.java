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
package com.aizuda.snailjob.server.common;

import java.util.List;

/**
 * 服务端负载均衡分配算法
 */
public interface ServerLoadBalance<T, D> {

    /**
     * 将waitAllocateList通过算法分配到currentCID中
     *
     * @param currentCID       当前节点的id
     * @param waitAllocateList 待分配的列表
     * @param nodeList         节点信息
     * @return 当前节点负责消费的桶数量
     */
    List<T> allocate(
            String currentCID,
            List<T> waitAllocateList,
            List<D> nodeList
    );

    /**
     * 算法名称
     *
     * @return 策略名称
     */
    String getName();
}
