package com.aizuda.snailjob.client.common.window;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.window.Listener;
import com.aizuda.snailjob.common.log.SnailJobLog;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 滑动窗口组件
 *
 * @author: opensnail
 * @date : 2023-07-23 13:38
 * @since 2.1.0
 */
public class SlidingWindow<T> {

    /**
     * 滑动窗口存储数据
     */
    public final TreeMap<LocalDateTime, ConcurrentLinkedQueue<T>> saveData = new TreeMap<>();

    /**
     * 总量窗口期阈值
     */
    private final Integer totalThreshold;

    /**
     * 开启的窗口数据预警
     */
    private final Integer windowTotalThreshold;

    /**
     * 监听器
     */
    private final List<Listener<T>> listeners;

    /**
     * 窗前期线程
     */
    private final ScheduledExecutorService threadPoolExecutor;

    /**
     * 窗口期时间长度
     */
    private final long duration;

    /**
     * 窗口期单位
     */
    private final ChronoUnit chronoUnit;

    /**
     * 新增窗口锁
     */
    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    /**
     * 到达时间窗口期或者总量窗口期锁
     */
    private static final ReentrantLock NOTICE_LOCK = new ReentrantLock();

    public SlidingWindow(int totalThreshold,
                         int windowTotalThreshold,
                         List<Listener<T>> listeners,
                         ScheduledExecutorService threadPoolExecutor,
                         long duration,
                         ChronoUnit chronoUnit) {
        this.totalThreshold = totalThreshold;
        this.listeners = listeners;
        this.windowTotalThreshold = windowTotalThreshold;
        this.threadPoolExecutor = threadPoolExecutor;
        this.duration = duration;
        this.chronoUnit = chronoUnit;
    }

    /**
     * 添加数据
     *
     * @param data 需要保存到窗口期内的数据
     */
    public void add(T data) {

        LocalDateTime now = LocalDateTime.now();
        if (isOpenNewWindow(now)) {

            SAVE_LOCK.lock();
            LocalDateTime windowPeriod = now.plus(duration, chronoUnit);
            try {

                // 防止开启两个间隔时间小于窗口期的窗口
                if (isOpenNewWindow(now)) {
                    ConcurrentLinkedQueue<T> list = new ConcurrentLinkedQueue<>();
                    list.add(data);

                    SnailJobLog.LOCAL.debug("Adding new data [{}] [{}] size:[{}]", windowPeriod, Thread.currentThread().getName(), list.size());
                    saveData.put(windowPeriod, list);

                    // 扫描n-1个窗口，是否过期，过期则删除
                    removeInvalidWindow();

                } else {
                    oldWindowAdd(data);
                }

            } finally {
                SAVE_LOCK.unlock();
            }

        } else {
            oldWindowAdd(data);
        }

    }

    /**
     * 超过窗口阈值预警
     */
    private void alarmWindowTotal() {
        if (saveData.size() > windowTotalThreshold) {
            SnailJobLog.LOCAL.warn(" The number of currently active windows is too high Total:[{}] > Threshold:[{}]", saveData.size(), windowTotalThreshold);
        }
    }

    /**
     * 扫描n-2个窗口，是否过期，过期则删除 过期条件为窗口期内无数据
     */
    private void removeInvalidWindow() {

        for (int i = 0; i < saveData.size() - 2; i++) {
            Map.Entry<LocalDateTime, ConcurrentLinkedQueue<T>> firstEntry = saveData.firstEntry();
            if (CollUtil.isEmpty(firstEntry.getValue())) {
                saveData.remove(firstEntry.getKey());
            }
        }
    }

    /**
     * 往已存在的窗口期内添加数据
     *
     * @param data 数据
     */
    private void oldWindowAdd(T data) {

        LocalDateTime windowPeriod = getNewWindowPeriod();
        if (Objects.isNull(windowPeriod)) {
            return;
        }

        // 高并发下情况下出现刚刚获取的最后一个窗口被删除的情况
        int count = 10;
        ConcurrentLinkedQueue<T> list = saveData.get(windowPeriod);
        while (Objects.isNull(list) && count > 0) {
            count--;
            windowPeriod = getNewWindowPeriod();
            if (Objects.isNull(windowPeriod)) {
                continue;
            }
            list = saveData.get(windowPeriod);
        }

        if (Objects.nonNull(list)) {
            list.add(data);
        } else {
            // 这里一般走不到，作为兜底
            SnailJobLog.LOCAL.error("Data loss. [{}]", JsonUtil.toJsonString(data));
        }

        if (list.size() >= totalThreshold) {
            doHandlerListener(windowPeriod);
        }

    }

    /**
     * 处理通知
     *
     * @param windowPeriod 窗口期时间
     */
    private void doHandlerListener(LocalDateTime windowPeriod) {

        NOTICE_LOCK.lock();
        // 深拷贝
        ConcurrentLinkedQueue<T> deepCopy = null;
        try {

            ConcurrentLinkedQueue<T> list = saveData.get(windowPeriod);
            if (CollUtil.isEmpty(list)) {
                return;
            }

            // 深拷贝
            deepCopy = new ConcurrentLinkedQueue<>(list);
            clear(windowPeriod, deepCopy);

            if (CollUtil.isEmpty(deepCopy)) {
                return;
            }

        } catch (Exception e) {
            SnailJobLog.LOCAL.error("deep copy task queue is error", e);
        } finally {
            NOTICE_LOCK.unlock();
        }

        if (!CollectionUtils.isEmpty(deepCopy)) {
            try {
                for (Listener<T> listener : listeners) {
                    listener.handler(new ArrayList<>(deepCopy));
                }
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("notice is error", e);
            }
        }
    }

    /**
     * 获取窗口期
     *
     * @return 窗口期时间
     */
    private LocalDateTime getOldWindowPeriod() {
        try {
            return saveData.firstKey();
        } catch (NoSuchElementException e) {
            SnailJobLog.LOCAL.error("First window exception. saveData:[{}]", JsonUtil.toJsonString(saveData));
            return null;
        }

    }

    /**
     * 获取窗口期
     *
     * @return 窗口期时间
     */
    private LocalDateTime getNewWindowPeriod() {
        try {
            return saveData.lastKey();
        } catch (NoSuchElementException e) {
            SnailJobLog.LOCAL.error("The last window is abnormal. SaveData:[{}]", JsonUtil.toJsonString(saveData));
            return null;
        }
    }

    /**
     * 删除2倍窗口期之前无效窗口
     *
     * @param windowPeriod 当前最老窗口期
     */
    private void removeInvalidWindow(LocalDateTime windowPeriod) {

        LocalDateTime currentTime = LocalDateTime.now().minus(duration * 2, chronoUnit);
        if (windowPeriod.isBefore(currentTime) && CollUtil.isEmpty(saveData.get(windowPeriod))) {
            saveData.remove(windowPeriod);
        }
    }

    /**
     * 是否开启新窗口期
     *
     * @return true- 开启 false- 关闭
     */
    private boolean isOpenNewWindow(LocalDateTime now) {

        if (saveData.isEmpty()) {
            return true;
        }

        LocalDateTime windowPeriod = getNewWindowPeriod();
        if (Objects.isNull(windowPeriod)) {
            return true;
        }

        return windowPeriod.isBefore(now);
    }

    /**
     * 提取存储的第一个数据进行判断是否到达窗口期
     *
     * @param condition 当前时间
     */
    private void extract(LocalDateTime condition) {

        if (saveData.isEmpty()) {
            return;
        }

        LocalDateTime windowPeriod = getOldWindowPeriod();
        if (Objects.isNull(windowPeriod)) {
            return;
        }

        // 删除过期窗口期数据
        removeInvalidWindow(windowPeriod);

        // 超过窗口阈值预警
        alarmWindowTotal();

        if (windowPeriod.isBefore(condition)) {
            SnailJobLog.LOCAL.debug("Time window reached [{}] [{}]", windowPeriod, JsonUtil.toJsonString(saveData));
            doHandlerListener(windowPeriod);
        }
    }

    /**
     * 清除已到达窗口期的数据
     *
     * @param windowPeriod 窗口期时间
     */
    private void clear(LocalDateTime windowPeriod, ConcurrentLinkedQueue<T> list) {
        saveData.get(windowPeriod).removeAll(list);
    }

    /**
     * 滑动窗口启动
     */
    public void start() {

        threadPoolExecutor.scheduleAtFixedRate(() -> {
            try {
                extract(LocalDateTime.now());
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Sliding window exception", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 滑动窗口关闭
     */
    public void end() {
        for (final LocalDateTime windowPeriod : saveData.keySet()) {
            doHandlerListener(windowPeriod);
        }
    }

    /**
     * 滑动窗口构建器
     *
     * @param <T>
     */
    public static class Builder<T> {

        /**
         * 总量窗口期阈值
         */
        private Integer totalThreshold = 10;

        /**
         * 窗口数量预警
         */
        private Integer windowTotalThreshold = 5;

        /**
         * 监听器
         */
        private List<Listener<T>> listeners;

        /**
         * 窗前期线程
         */
        private ScheduledExecutorService threadPoolExecutor;

        /**
         * 窗口期时间长度
         */
        private long duration = 10;

        /**
         * 窗口期单位
         */
        private ChronoUnit chronoUnit = ChronoUnit.SECONDS;

        /**
         * 创建一个新的构建器
         *
         * @param <T>
         * @return this
         */
        public static <T> Builder<T> newBuilder() {
            return new Builder<T>();
        }

        /**
         * 总量窗口期阈值
         *
         * @param totalThreshold 总量窗口期阈值
         * @return this
         */
        public Builder<T> withTotalThreshold(int totalThreshold) {
            Assert.isTrue(totalThreshold > 0, "Total window period threshold cannot be less than 0");
            this.totalThreshold = totalThreshold;
            return this;
        }

        /**
         * 窗口数量预警
         *
         * @param windowTotalThreshold 窗口数量阈值
         * @return this
         */
        public Builder<T> withWindowTotalThreshold(int windowTotalThreshold) {
            Assert.isTrue(windowTotalThreshold > 0, "Window quantity threshold cannot be less than 0");
            this.windowTotalThreshold = windowTotalThreshold;
            return this;
        }

        /**
         * 添加监听器
         *
         * @param listener 监听器
         * @return this
         */
        public Builder<T> withListener(Listener<T> listener) {

            if (CollUtil.isEmpty(listeners)) {
                listeners = new ArrayList<>();
            }

            listeners.add(listener);
            return this;
        }

        /**
         * 添加窗口期时间
         *
         * @param duration   时长
         * @param chronoUnit 单位
         * @return this
         */
        public Builder<T> withDuration(long duration, ChronoUnit chronoUnit) {
            Assert.isTrue(duration > 0, "Window period cannot be less than 0");
            this.duration = duration;
            this.chronoUnit = chronoUnit;
            return this;
        }

        /**
         * 添加定时调度线程池
         *
         * @param threadPoolExecutor 线程池对象
         * @return this
         */
        public Builder<T> withScheduledExecutorServiced(ScheduledExecutorService threadPoolExecutor) {
            this.threadPoolExecutor = threadPoolExecutor;
            return this;
        }

        /**
         * 构建滑动窗口对象
         *
         * @return {@link SlidingWindow} 滑动窗口对象
         */
        public SlidingWindow<T> build() {
            if (Objects.isNull(threadPoolExecutor)) {
                threadPoolExecutor = Executors
                        .newSingleThreadScheduledExecutor(r -> new Thread(r, "sliding-window-thread"));
            }

            if (CollUtil.isEmpty(listeners)) {
                listeners = Collections.EMPTY_LIST;
            }

            return new SlidingWindow<>(totalThreshold, windowTotalThreshold, listeners, threadPoolExecutor, duration,
                    chronoUnit);
        }

    }
}
