package com.aizuda.easy.retry.server.support.idempotent;

import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.support.IdempotentStrategy;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * BitSet幂等校验器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-23 09:26
 */
@Component
public class BitSetIdempotentStrategyHandler implements IdempotentStrategy<String, Integer> {
    /**
     * BIT_SET_MAP[key] : group
     * BIT_SET_MAP[value] : BitSet
     */
    public static final Map<String, BitSet> BIT_SET_MAP = new HashMap<>();

    @Override
    public boolean set(String groupId, Integer key) {

        BitSet bitSet = BIT_SET_MAP.get(groupId);
        if (Objects.isNull(bitSet)) {

            bitSet = new BitSet(16);
            bitSet.set(key, Boolean.TRUE);
            BIT_SET_MAP.put(groupId, bitSet);
        }

        return Boolean.TRUE;
    }

    @Override
    public Integer get(String s) {
        throw new EasyRetryServerException("不支持的操作");
    }

    @Override
    public boolean isExist(String groupId, Integer value) {
        BitSet bitSet = BIT_SET_MAP.getOrDefault(groupId, new BitSet(16));
        return bitSet.get(value);
    }

    @Override
    public boolean clear(String groupId, Integer value) {
        BitSet bitSet = BIT_SET_MAP.get(groupId);
        bitSet.clear(value);
        return Boolean.TRUE;
    }

}
