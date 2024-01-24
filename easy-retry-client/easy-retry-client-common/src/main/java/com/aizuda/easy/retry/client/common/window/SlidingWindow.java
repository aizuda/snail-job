package com.aizuda.easy.retry.client.common.window;

import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.core.window.Listener;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
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
 * @author: www.byteblogs.com
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

                    EasyRetryLog.LOCAL.info("添加新数据 [{}] [{}] size:[{}]", windowPeriod, Thread.currentThread().getName(), list.size());
                    saveData.put(windowPeriod, list);

                    // 扫描n-1个窗口，是否过期，过期则删除
                    removeInvalidWindow();

                    // 超过窗口阈值预警
                    alarmWindowTotal();

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
            EasyRetryLog.LOCAL.warn("当前存活的窗口数量过多 总量:[{}] > 阈值:[{}] ", saveData.size(), windowTotalThreshold);
        }
    }

    /**
     * 扫描n-1个窗口，是否过期，过期则删除 过期条件为窗口期内无数据
     */
    private void removeInvalidWindow() {

        for (int i = 0; i < saveData.size() - 1; i++) {
            Map.Entry<LocalDateTime, ConcurrentLinkedQueue<T>> firstEntry = saveData.firstEntry();
            if (CollectionUtils.isEmpty(firstEntry.getValue())) {
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

        ConcurrentLinkedQueue<T> list = saveData.get(windowPeriod);
        list.add(data);

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

        try {

            ConcurrentLinkedQueue<T> list = saveData.get(windowPeriod);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            // 深拷贝
            ConcurrentLinkedQueue<T> deepCopy = new ConcurrentLinkedQueue<>(list);
            clear(windowPeriod, deepCopy);

            if (CollectionUtils.isEmpty(deepCopy)) {
                return;
            }

            for (Listener<T> listener : listeners) {
                listener.handler(new ArrayList<>(deepCopy));
            }

        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("到达总量窗口期通知异常", e);
        } finally {
            NOTICE_LOCK.unlock();
        }

    }

    /**
     * 删除2倍窗口期之前无效窗口
     *
     * @param windowPeriod 当前最老窗口期
     */
    private void removeInvalidWindow(LocalDateTime windowPeriod) {

        LocalDateTime currentTime = LocalDateTime.now().minus(duration * 2, chronoUnit);
        if (windowPeriod.isBefore(currentTime)) {
            EasyRetryLog.LOCAL.info("删除过期窗口 windowPeriod:[{}] currentTime:[{}]", windowPeriod, currentTime);
            saveData.remove(windowPeriod);
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
            EasyRetryLog.LOCAL.error("第一个窗口异常. saveData:[{}]", JsonUtil.toJsonString(saveData));
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
            EasyRetryLog.LOCAL.error("第后一个窗口异常. saveData:[{}]", JsonUtil.toJsonString(saveData));
            return null;
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

        if (windowPeriod.isBefore(condition)) {
            EasyRetryLog.LOCAL.info("到达时间窗口期 [{}] [{}]", windowPeriod, JsonUtil.toJsonString(saveData));
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
                extract(LocalDateTime.now().minus(duration, chronoUnit));
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("滑动窗口异常", e);
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
            Assert.isTrue(totalThreshold > 0, "总量窗口期阈值不能小于0");
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
            Assert.isTrue(windowTotalThreshold > 0, "窗口数量阈值不能小于0");
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

            if (CollectionUtils.isEmpty(listeners)) {
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
            Assert.isTrue(duration > 0, "窗口期不能小于0");
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

            if (CollectionUtils.isEmpty(listeners)) {
                listeners = Collections.EMPTY_LIST;
            }

            return new SlidingWindow<>(totalThreshold, windowTotalThreshold, listeners, threadPoolExecutor, duration,
                chronoUnit);
        }

    }
}
