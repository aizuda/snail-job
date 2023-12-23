package com.aizuda.easy.retry.server.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.io.IOException;
import java.util.Map;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22
 * @since : 2.6.0
 */
public class GraphUtils {


    // 从JSON反序列化为Guava图
    public static <T> MutableGraph<T> deserializeJsonToGraph(String jsonGraph) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 将JSON字符串转换为Map<String, Iterable<String>>
        Map<T, Iterable<T>> adjacencyList = objectMapper.readValue(
            jsonGraph, new TypeReference<Map<T, Iterable<T>>>() {});

        // 创建Guava图并添加节点和边
        MutableGraph<T> graph = GraphBuilder.directed().build();
        for (Map.Entry<T, Iterable<T>> entry : adjacencyList.entrySet()) {
            T node = entry.getKey();
            Iterable<T> successors = entry.getValue();

            graph.addNode(node);
            for (T successor : successors) {
                graph.putEdge(node, successor);
            }
        }

        return graph;
    }
}
