package com.aizuda.easy.retry.server.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22
 * @since : 2.6.0
 */
public class GraphUtils {


    // 从JSON反序列化为Guava图
    public static MutableGraph<Long> deserializeJsonToGraph(String jsonGraph) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 将JSON字符串转换为Map<Long, Iterable<Long>>
        Map<Long, Iterable<Long>> adjacencyList = objectMapper.readValue(
                jsonGraph, new TypeReference<Map<Long, Iterable<Long>>>() {
                });

        // 创建Guava图并添加节点和边
        MutableGraph<Long> graph = GraphBuilder.directed().build();
        for (Map.Entry<Long, Iterable<Long>> entry : adjacencyList.entrySet()) {
            Long node = entry.getKey();
            Iterable<Long> successors = entry.getValue();

            graph.addNode(node);
            for (Long successor : successors) {
                graph.putEdge(node, successor);
            }
        }

        return graph;
    }

    public static Map<Long, Iterable<Long>> serializeGraphToJson(MutableGraph<Long> graph) {
        Map<Long, Iterable<Long>> adjacencyList = new HashMap<>();

        for (Long node : graph.nodes()) {
            adjacencyList.put(node, graph.successors(node));
        }

        return adjacencyList;
    }

}
