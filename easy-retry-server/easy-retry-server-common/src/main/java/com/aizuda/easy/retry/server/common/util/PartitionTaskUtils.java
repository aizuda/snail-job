package com.aizuda.easy.retry.server.common.util;

import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-11 10:58
 * @since : 2.4.0
 */
public class PartitionTaskUtils {

    private PartitionTaskUtils() {
    }

    public static long process(
            Function<Long, List<? extends PartitionTask>> dataSource, Consumer<List<? extends PartitionTask>> task,
            long startId) {
        int total = 0;
        do {
            List<? extends PartitionTask> products = dataSource.apply(startId);
            if (CollectionUtils.isEmpty(products)) {
                // 没有查询到数据直接退出
                break;
            }

            total += products.size();

            task.accept(products);
            startId = maxId(products);
        } while (startId > 0);

        return total;
    }

    private static long maxId(List<? extends PartitionTask> products) {
        // 使用的地方必须按照id正序排序
        return products.get(products.size() - 1).getId() + 1;
    }

}
