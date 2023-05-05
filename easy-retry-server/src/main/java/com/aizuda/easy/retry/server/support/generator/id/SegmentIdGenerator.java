package com.aizuda.easy.retry.server.support.generator.id;

import com.aizuda.easy.retry.common.core.enums.IdGeneratorMode;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.SequenceAllocMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.SequenceAlloc;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.aizuda.easy.retry.server.support.generator.IdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 此算法来自美团的leaf号段模式
 *
 * @author www.byteblogs.com
 * @date 2023-05-04
 * @since 1.2.0
 */
@Component
@Slf4j
public class SegmentIdGenerator implements IdGenerator, Lifecycle {

    /**
     * IDCache未初始化成功时的异常码
     */
    private static final long EXCEPTION_ID_IDCACHE_INIT_FALSE = -1;
    /**
     * key不存在时的异常码
     */
    private static final long EXCEPTION_ID_KEY_NOT_EXISTS = -2;
    /**
     * SegmentBuffer中的两个Segment均未从DB中装载时的异常码
     */
    private static final long EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL = -3;
    /**
     * 最大步长不超过100,0000
     */
    private static final int MAX_STEP = 1000000;
    /**
     * 一个Segment维持时间为15分钟
     */
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;

    private ThreadPoolExecutor service = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS,
        new LinkedBlockingDeque<>(5000), new UpdateThreadFactory());

    private volatile boolean initOK = false;
    private Map<String, SegmentBuffer> cache = new ConcurrentHashMap<>();

    @Autowired
    private SequenceAllocMapper sequenceAllocMapper;

    @Override
    public void start() {
        LogUtils.info(log, "SegmentIdGenerator start");
        // 确保加载到kv后才初始化成功
        updateCacheFromDb();
        initOK = true;
        updateCacheFromDbAtEveryMinute();
        LogUtils.info(log, "SegmentIdGenerator start end");
    }

    @Override
    public void close() {
        LogUtils.info(log, "SegmentIdGenerator close");
    }

    public static class UpdateThreadFactory implements ThreadFactory {

        private static int threadInitNumber = 0;

        private static synchronized int nextThreadNum() {
            return threadInitNumber++;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Thread-Segment-Update-" + nextThreadNum());
        }
    }

    private void updateCacheFromDbAtEveryMinute() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("check-id-cache-thread");
            t.setDaemon(true);
            return t;
        });
        service.scheduleWithFixedDelay(() -> updateCacheFromDb(), 60, 60, TimeUnit.SECONDS);
    }

    private void updateCacheFromDb() {
        LogUtils.info(log, "update cache from db");
        StopWatch sw = new Slf4JStopWatch();
        try {
            List<SequenceAlloc> sequenceAllocs = sequenceAllocMapper
                .selectList(new LambdaQueryWrapper<SequenceAlloc>().select(SequenceAlloc::getGroupName));
            if (CollectionUtils.isEmpty(sequenceAllocs)) {
                return;
            }

            List<String> dbTags = sequenceAllocs.stream().map(SequenceAlloc::getGroupName).collect(Collectors.toList());

            List<String> cacheTags = new ArrayList<>(cache.keySet());
            Set<String> insertTagsSet = new HashSet<>(dbTags);
            Set<String> removeTagsSet = new HashSet<>(cacheTags);
            //db中新加的tags灌进cache
            for(int i = 0; i < cacheTags.size(); i++){
                String tmp = cacheTags.get(i);
                if(insertTagsSet.contains(tmp)){
                    insertTagsSet.remove(tmp);
                }
            }
            for (String tag : insertTagsSet) {
                SegmentBuffer buffer = new SegmentBuffer();
                buffer.setKey(tag);
                Segment segment = buffer.getCurrent();
                segment.setValue(new AtomicLong(0));
                segment.setMax(0);
                segment.setStep(0);
                cache.put(tag, buffer);
                LogUtils.info(log, "Add tag {} from db to IdCache, SegmentBuffer {}", tag, buffer);
            }
            //cache中已失效的tags从cache删除
            for(int i = 0; i < dbTags.size(); i++){
                String tmp = dbTags.get(i);
                if(removeTagsSet.contains(tmp)){
                    removeTagsSet.remove(tmp);
                }
            }
            for (String tag : removeTagsSet) {
                cache.remove(tag);
                LogUtils.info(log, "Remove tag {} from IdCache", tag);
            }
        } catch (Exception e) {
            LogUtils.warn(log, "update cache from db exception", e);
        } finally {
            sw.stop("updateCacheFromDb");
        }
    }

    public String get(final String key) {
        if (!initOK) {
            return Long.toString(EXCEPTION_ID_IDCACHE_INIT_FALSE);
        }

        if (cache.containsKey(key)) {
            SegmentBuffer buffer = cache.get(key);
            if (!buffer.isInitOk()) {
                synchronized (buffer) {
                    if (!buffer.isInitOk()) {
                        try {
                            updateSegmentFromDb(key, buffer.getCurrent());
                            LogUtils.info(log, "Init buffer. Update leafkey {} {} from db", key, buffer.getCurrent());
                            buffer.setInitOk(true);
                        } catch (Exception e) {
                            LogUtils.warn(log, "Init buffer {} exception", buffer.getCurrent(), e);
                        }
                    }
                }
            }
            return getIdFromSegmentBuffer(cache.get(key));
        }
        return Long.toString(EXCEPTION_ID_KEY_NOT_EXISTS);
    }

    public void updateSegmentFromDb(String key, Segment segment) {
        StopWatch sw = new Slf4JStopWatch();
        SegmentBuffer buffer = segment.getBuffer();
        SequenceAlloc sequenceAlloc;
        if (!buffer.isInitOk()) {
            sequenceAllocMapper.updateMaxId(key);
            sequenceAlloc = sequenceAllocMapper.selectOne(new LambdaQueryWrapper<SequenceAlloc>().eq(SequenceAlloc::getGroupName, key));
            buffer.setStep(sequenceAlloc.getStep());
            buffer.setMinStep(sequenceAlloc.getStep());//leafAlloc中的step为DB中的step
        } else if (buffer.getUpdateTimestamp() == 0) {
            sequenceAllocMapper.updateMaxId(key);
            sequenceAlloc = sequenceAllocMapper.selectOne(new LambdaQueryWrapper<SequenceAlloc>().eq(SequenceAlloc::getGroupName, key));
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(sequenceAlloc.getStep());
            buffer.setMinStep(sequenceAlloc.getStep());//leafAlloc中的step为DB中的step
        } else {
            long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
            int nextStep = buffer.getStep();
            if (duration < SEGMENT_DURATION) {
                if (nextStep * 2 > MAX_STEP) {
                    //do nothing
                } else {
                    nextStep = nextStep * 2;
                }
            } else if (duration < SEGMENT_DURATION * 2) {
                //do nothing with nextStep
            } else {
                nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
            }
            LogUtils.info(log,"leafKey[{}], step[{}], duration[{}mins], nextStep[{}]", key, buffer.getStep(), String.format("%.2f",((double)duration / (1000 * 60))), nextStep);

            sequenceAllocMapper.updateMaxIdByCustomStep(nextStep, key);
            sequenceAlloc = sequenceAllocMapper
                .selectOne(new LambdaQueryWrapper<SequenceAlloc>().eq(SequenceAlloc::getGroupName, key));
            buffer.setUpdateTimestamp(System.currentTimeMillis());
            buffer.setStep(nextStep);
            buffer.setMinStep(sequenceAlloc.getStep());//leafAlloc的step为DB中的step
        }
        // must set value before set max
        long value = sequenceAlloc.getMaxId() - buffer.getStep();
        segment.getValue().set(value);
        segment.setMax(sequenceAlloc.getMaxId());
        segment.setStep(buffer.getStep());
        sw.stop("updateSegmentFromDb", key + " " + segment);
    }

    public String getIdFromSegmentBuffer(final SegmentBuffer buffer) {
        while (true) {
            buffer.rLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                if (!buffer.isNextReady() && (segment.getIdle() < 0.9 * segment.getStep()) && buffer.getThreadRunning().compareAndSet(false, true)) {
                    service.execute(() -> {
                        Segment next = buffer.getSegments()[buffer.nextPos()];
                        boolean updateOk = false;
                        try {
                            updateSegmentFromDb(buffer.getKey(), next);
                            updateOk = true;
                            LogUtils.info(log,"update segment {} from db {}", buffer.getKey(), next);
                        } catch (Exception e) {
                            LogUtils.warn(log, buffer.getKey() + " updateSegmentFromDb exception", e);
                        } finally {
                            if (updateOk) {
                                buffer.wLock().lock();
                                buffer.setNextReady(true);
                                buffer.getThreadRunning().set(false);
                                buffer.wLock().unlock();
                            } else {
                                buffer.getThreadRunning().set(false);
                            }
                        }
                    });
                }
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return Long.toString(value);
                }
            } finally {
                buffer.rLock().unlock();
            }
            waitAndSleep(buffer);
            buffer.wLock().lock();
            try {
                final Segment segment = buffer.getCurrent();
                long value = segment.getValue().getAndIncrement();
                if (value < segment.getMax()) {
                    return Long.toString(value);
                }
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    LogUtils.error(log,"Both two segments in {} are not ready!", buffer);
                    return Long.toString(EXCEPTION_ID_TWO_SEGMENTS_ARE_NULL);
                }
            } finally {
                buffer.wLock().unlock();
            }
        }
    }

    private void waitAndSleep(SegmentBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if(roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(2);
                    break;
                } catch (InterruptedException e) {
                    LogUtils.warn(log,"Thread {} Interrupted",Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

    @Override
    public boolean supports(int mode) {
        return IdGeneratorMode.SEGMENT.getMode() == mode;
    }

    @Override
    public String idGenerator(String group) {
        return get(group);
    }

}
