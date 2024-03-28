package com.aizuda.easy.retry.server.common.generator.id;

import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.enums.IdGeneratorModeEnum;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SequenceAllocMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.SequenceAlloc;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 特别声明: 此算法来自美团的leaf号段模式
 * see： https://github.com/Meituan-Dianping/Leaf/blob/master/leaf-server/src/main/java/com/sankuai/inf/leaf/server/service/SegmentService.java
 *
 *  @author www.byteblogs.com
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
    private Map<Pair<String, String>, SegmentBuffer> cache = new ConcurrentHashMap<>();

    @Autowired
    private SequenceAllocMapper sequenceAllocMapper;

    @Override
    public void start() {
       EasyRetryLog.LOCAL.info("SegmentIdGenerator start");
        // 确保加载到kv后才初始化成功
        updateCacheFromDb();
        initOK = true;
        updateCacheFromDbAtEveryMinute();
       EasyRetryLog.LOCAL.info("SegmentIdGenerator start end");
    }

    @Override
    public void close() {
       EasyRetryLog.LOCAL.info("SegmentIdGenerator close");
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
        service.scheduleWithFixedDelay(this::updateCacheFromDb, 60, 60, TimeUnit.SECONDS);
    }

    private void updateCacheFromDb() {
        try {
            List<SequenceAlloc> sequenceAllocs = sequenceAllocMapper
                    .selectList(new LambdaQueryWrapper<SequenceAlloc>().select(SequenceAlloc::getGroupName));
            if (CollectionUtils.isEmpty(sequenceAllocs)) {
                return;
            }

            List<Pair<String, String>> dbTags = sequenceAllocs.stream()
                    .map(sequenceAlloc -> Pair.of(sequenceAlloc.getGroupName(), sequenceAlloc.getNamespaceId()))
                    .collect(Collectors.toList());

            List<Pair<String, String>> cacheTags = new ArrayList<>(cache.keySet());
            Set<Pair<String, String>> insertTagsSet = new HashSet<>(dbTags);
            Set<Pair<String, String>> removeTagsSet = new HashSet<>(cacheTags);
            //db中新加的tags灌进cache
            for (int i = 0; i < cacheTags.size(); i++) {
                Pair<String, String> tmp = cacheTags.get(i);
                if (insertTagsSet.contains(tmp)) {
                    insertTagsSet.remove(tmp);
                }
            }
            for (Pair<String, String> tag : insertTagsSet) {
                SegmentBuffer buffer = new SegmentBuffer();
                buffer.setKey(tag);
                Segment segment = buffer.getCurrent();
                segment.setValue(new AtomicLong(0));
                segment.setMax(0);
                segment.setStep(0);
                cache.put(tag, buffer);
               EasyRetryLog.LOCAL.debug("Add tag {} from db to IdCache, SegmentBuffer {}", tag, buffer);
            }
            //cache中已失效的tags从cache删除
            for (int i = 0; i < dbTags.size(); i++) {
                Pair<String, String> tmp = dbTags.get(i);
                if (removeTagsSet.contains(tmp)) {
                    removeTagsSet.remove(tmp);
                }
            }
            for (Pair<String, String> tag : removeTagsSet) {
                cache.remove(tag);
               EasyRetryLog.LOCAL.debug("Remove tag {} from IdCache", tag);
            }
        } catch (Exception e) {
           EasyRetryLog.LOCAL.error("update cache from db exception", e);
        }
    }

    public String get(final String groupName, String namespaceId) {
        if (!initOK) {
            return Long.toString(EXCEPTION_ID_IDCACHE_INIT_FALSE);
        }

        Pair<String, String> key = Pair.of(groupName, namespaceId);
        if (cache.containsKey(key)) {
            SegmentBuffer buffer = cache.get(key);
            if (!buffer.isInitOk()) {
                synchronized (buffer) {
                    if (!buffer.isInitOk()) {
                        try {
                            updateSegmentFromDb(key, buffer.getCurrent());
                            EasyRetryLog.LOCAL.debug("Init buffer. Update key {} {} from db", key, buffer.getCurrent());
                            buffer.setInitOk(true);
                        } catch (Exception e) {
                           EasyRetryLog.LOCAL.error("Init buffer {} exception", buffer.getCurrent(), e);
                        }
                    }
                }
            }
            return getIdFromSegmentBuffer(cache.get(key));
        }
        return Long.toString(EXCEPTION_ID_KEY_NOT_EXISTS);
    }

    public void updateSegmentFromDb(Pair<String, String> key, Segment segment) {
        SegmentBuffer buffer = segment.getBuffer();
        SequenceAlloc sequenceAlloc;
        LambdaUpdateWrapper<SequenceAlloc> wrapper = new LambdaUpdateWrapper<SequenceAlloc>()
                .set(SequenceAlloc::getMaxId, "max_id + step")
                .set(SequenceAlloc::getUpdateDt, new Date())
                .eq(SequenceAlloc::getGroupName, key.getKey())
                .eq(SequenceAlloc::getNamespaceId, key.getValue());
        if (!buffer.isInitOk()) {
            sequenceAllocMapper.update(wrapper);
            sequenceAlloc = sequenceAllocMapper.selectOne(new LambdaQueryWrapper<SequenceAlloc>().eq(SequenceAlloc::getGroupName, key));
            buffer.setStep(sequenceAlloc.getStep());
            buffer.setMinStep(sequenceAlloc.getStep());//leafAlloc中的step为DB中的step
        } else if (buffer.getUpdateTimestamp() == 0) {
            sequenceAllocMapper.update(wrapper);
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
            EasyRetryLog.LOCAL.debug("leafKey[{}], step[{}], duration[{}mins], nextStep[{}]", key, buffer.getStep(), String.format("%.2f", ((double) duration / (1000 * 60))), nextStep);
            LambdaUpdateWrapper<SequenceAlloc> wrapper1 = new LambdaUpdateWrapper<SequenceAlloc>()
                    .set(SequenceAlloc::getMaxId, "max_id + " + nextStep)
                    .set(SequenceAlloc::getUpdateDt, new Date())
                    .eq(SequenceAlloc::getGroupName, key.getKey())
                    .eq(SequenceAlloc::getNamespaceId, key.getValue());
            sequenceAllocMapper.update(wrapper1);
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
                           EasyRetryLog.LOCAL.debug("update segment {} from db {}", buffer.getKey(), next);
                        } catch (Exception e) {
                           EasyRetryLog.LOCAL.warn(buffer.getKey() + " updateSegmentFromDb exception", e);
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
                    EasyRetryLog.LOCAL.error("Both two segments in {} are not ready!", buffer);
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
            if (roll > 10000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                    break;
                } catch (InterruptedException e) {
                   EasyRetryLog.LOCAL.warn("Thread {} Interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }

    @Override
    public boolean supports(int mode) {
        return IdGeneratorModeEnum.SEGMENT.getMode() == mode;
    }

    @Override
    public String idGenerator(String groupName, String namespaceId) {
        String time = DateUtils.format(DateUtils.toNowLocalDateTime(), DateUtils.PURE_DATETIME_MS_PATTERN);
        return time.concat(get(groupName, namespaceId));
    }

}
