package com.aizuda.snailjob.client.common.window;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A sliding window implementation that maintains data in a ring buffer structure.
 * The window slides based on either time duration or threshold count, whichever comes first.
 *
 * <p>Key features:
 * <ul>
 *   <li>Thread-safe implementation using atomic operations</li>
 *   <li>Supports both time-based and count-based window sliding</li>
 *   <li>Uses a ring buffer to efficiently manage window segments</li>
 *   <li>Allows multiple listeners to be notified when window data is emitted</li>
 * </ul>
 *
 * @param <T> the type of elements maintained in the window
 */
@Slf4j
public class SlidingRingWindow<T> {

    private final AtomicReferenceArray<Window<T>> ringArray;

    private final Duration duration;

    private final Integer totalThreshold;

    private final List<Consumer<List<T>>> listener;

    private final AtomicLong sequencer = new AtomicLong();

    private final ScheduledExecutorService scheduler;

    private static final int DEFAULT_RING_SIZE = 4;

    public SlidingRingWindow(Duration duration, Integer totalThreshold, List<Consumer<List<T>>> listener) {
        this(DEFAULT_RING_SIZE, duration, totalThreshold, listener, new ScheduledThreadPoolExecutor(1));
    }


    public SlidingRingWindow(int ringSize, Duration duration, Integer totalThreshold, List<Consumer<List<T>>> listener) {
        this(ringSize, duration, totalThreshold, listener, new ScheduledThreadPoolExecutor(1));
    }

    public SlidingRingWindow(int ringSize, Duration duration, Integer totalThreshold, List<Consumer<List<T>>> listener, ScheduledExecutorService scheduler) {
        this.duration = duration;
        this.totalThreshold = totalThreshold;
        this.ringArray = new AtomicReferenceArray<>(ringSize);
        this.listener = listener;
        this.scheduler = scheduler;
    }

    /**
     * Adds an element to the current window.
     * <p>This operation may trigger window sliding if either:
     * <ul>
     *   <li>The current window's time has expired</li>
     *   <li>The current window's element count has reached the threshold</li>
     * </ul>
     *
     * @param data the element to be added to the window
     */
    public void add(T data) {
        final var now = LocalDateTime.now();

        for (; ; ) {
            final long currentSequence = sequencer.get();
            final Window<T> currentWindow = getWindow(currentSequence);

            if (currentWindow == null) {
                /*
                 * 情况1: 环形数组初始状态(全空)
                 * [NULL] [NULL] [NULL] [NULL]
                 *   ^
                 *  从第一个槽位(sequence=0)开始创建窗口
                 */
                final Window<T> newWindow = new Window<>(now.plus(duration), ConcurrentLinkedQueue::new);
                if (createNewWindow(currentSequence, null, newWindow)) {
                    // add new
                    newWindow.getQueue().add(data);
                    // schedule time window emit after add new window
                    scheduleWindowEmit(newWindow, now);
                    return;
                }
            } else if (currentWindow.isOutWindow(now)) {
                /*
                 * 情况2: 当前窗口已过期
                 *   W1(过期)   W2     NULL    NULL
                 * |________|________|______|______|
                 *     ^          ^
                 *  当前窗口      下一个窗口位置
                 *
                 * 情况3: 环形数组循环覆盖
                 *   W5(过期)  [W2→W6]   W3    W4
                 * |________|________|________|________|
                 *     ^        ^
                 *   sequence=5 (5 % 4 = 1)
                 *   在原本W2的位置创建W6
                 *   并清理过期的W5
                 */
                final Window<T> newWindow = new Window<>(now.plus(duration), ConcurrentLinkedQueue::new);
                final long nextSequence = currentSequence + 1;
                // maybe next is old
                final Window<T> nextWindow = getWindow(nextSequence);
                // next window might be swapped(is new window now), so must verify it
                if ((nextWindow == null || nextWindow.isOutWindow(now)) && createNewWindow(nextSequence, nextWindow, newWindow)) {
                    //update sequence, must increase first
                    sequencer.incrementAndGet();
                    // add new
                    newWindow.getQueue().add(data);
                    // schedule time window emit after add new window
                    scheduleWindowEmit(newWindow, now);
                    // clear old
                    if (nextWindow != null) {
                        emit(nextWindow);
                    }
                    return;
                }
            } else {
                /*
                 * 情况4: 当前窗口活跃
                 *   W1(活跃)   W2     NULL    NULL
                 * |________|________|______|______|
                 *     ^
                 *  当前窗口未过期且未满
                 *
                 * 情况5: 达到阈值触发
                 *   W1[元素A,B,C] → 添加D时达到阈值
                 * |________________|
                 *     ^
                 *  立即触发emit
                 */
                final var queue = currentWindow.getQueue();
                queue.add(data);
                if (queue.size() >= totalThreshold) {
                    emit(currentWindow);
                }
                return;
            }
        }
    }


    private void scheduleWindowEmit(Window<T> window, LocalDateTime now) {
        scheduler.schedule(() -> emit(window)
                , now.until(window.windowTime, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
    }


    /**
     * Emits all elements from the specified window to registered listeners.
     * <p>This is an internal method that handles the actual data emission when
     * a window is closed (either by time or count threshold).
     *
     * @param window the window whose elements should be emitted
     */
    private void emit(Window<T> window) {
        final var drainList = window.drain();
        if (drainList.isEmpty()) {
            return;
        }
        try {
            listener.forEach(consumer -> consumer.accept(drainList));
        } catch (Throwable e) {
            log.error("sliding window emit is error", e);
        }

    }

    private boolean createNewWindow(long sequence, Window<T> oldWindow, Window<T> newWindow) {
        final var index = getIndex(sequence);
        return ringArray.compareAndSet(index, oldWindow, newWindow);
    }

    private Window<T> getWindow(long sequence) {
        final var index = getIndex(sequence);
        return ringArray.get(index);
    }

    private int getIndex(long sequence) {
        return (int) (sequence % ringArray.length());
    }

    /**
     * Internal window class that maintains data for a specific time period.
     * <p>Each window instance has:
     * <ul>
     *   <li>An expiration time</li>
     *   <li>A thread-safe queue for storing elements</li>
     *   <li>Mechanism to drain elements atomically</li>
     * </ul>
     */
    private static final class Window<T> {

        private final LocalDateTime windowTime;

        private final Supplier<ConcurrentLinkedQueue<T>> dataQueueSupplier;

        private volatile ConcurrentLinkedQueue<T> dataQueue;

        private final AtomicInteger wip = new AtomicInteger();

        private Window(LocalDateTime windowTime, Supplier<ConcurrentLinkedQueue<T>> dataQueueSupplier) {
            this.windowTime = windowTime;
            this.dataQueueSupplier = dataQueueSupplier;
        }


        public boolean isOutWindow(LocalDateTime now) {
            return windowTime.isBefore(now);
        }

        public ConcurrentLinkedQueue<T> getQueue() {
            if (dataQueue == null) {
                synchronized (this) {
                    if (dataQueue == null) {
                        dataQueue = dataQueueSupplier.get();
                    }
                }
            }
            return dataQueue;
        }

        public List<T> drain() {
            if (wip.getAndAdd(1) == 0) {
                final List<T> list = new ArrayList<>();

                var missed = -1;
                do {
                    T element;
                    while ((element = dataQueue.poll()) != null) {
                        list.add(element);
                    }
                } while ((missed = wip.addAndGet(-missed)) != 0);

                return list;
            }
            return List.of();
        }
    }

}