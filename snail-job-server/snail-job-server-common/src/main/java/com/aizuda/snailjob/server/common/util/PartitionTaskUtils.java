package com.aizuda.snailjob.server.common.util;

import com.aizuda.snailjob.server.common.dto.PartitionTask;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;

/**
 * @author: opensnail
 * @date : 2023-10-11 10:58
 * @since : 2.4.0
 */
public class PartitionTaskUtils {

    private PartitionTaskUtils() {
    }

    /**
     * 使用此方法必须order id asc
     *
     * @param dataSource 数据来源
     * @param task 执行任务
     * @param startId 初始id
     * @return 总任务数
     */
    public static long process(LongFunction<List<? extends PartitionTask>> dataSource,
                               Consumer<List<? extends PartitionTask>> task,
                               long startId) {
        return process(dataSource, task, curStartId -> {
        }, CollectionUtils::isEmpty, startId);
    }

    public static long process(LongFunction<List<? extends PartitionTask>> dataSource,
                               Consumer<List<? extends PartitionTask>> task,
                               Predicate<List<? extends PartitionTask>> stopCondition,
                               long startId) {
        return process(dataSource, task, curStartId -> {
        }, stopCondition, startId);
    }

    public static long process(LongFunction<List<? extends PartitionTask>> dataSource,
                               Consumer<List<? extends PartitionTask>> task,
                               LongConsumer stopAfterProcessor,
                               long startId) {
        return process(dataSource, task, stopAfterProcessor, CollectionUtils::isEmpty, startId);
    }

    public static long process(
            LongFunction<List<? extends PartitionTask>> dataSource,
            Consumer<List<? extends PartitionTask>> task,
            LongConsumer stopAfterProcessor,
            Predicate<List<? extends PartitionTask>> stopCondition,
            long startId) {
        int total = 0;
        do {
            List<? extends PartitionTask> products = dataSource.apply(startId);
            if (stopCondition.test(products)) {
                // 没有查询到数据直接退出
                stopAfterProcessor.accept(startId);
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
