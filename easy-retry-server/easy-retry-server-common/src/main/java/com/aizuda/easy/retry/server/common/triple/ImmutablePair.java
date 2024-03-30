package com.aizuda.easy.retry.server.common.triple;

import com.aizuda.easy.retry.common.core.exception.EasyRetryCommonException;

import java.io.Serial;

/**
 * @author: xiaowoniu
 * @date : 2024-03-30
 * @since : 3.2.0
 */
public final class ImmutablePair<L, R> extends Pair<L, R>{

    @Serial
    private static final long serialVersionUID = 4954918890077093841L;

    public final L left;
    public final R right;


    public static <L, R> ImmutablePair<L, R> of(final L left, final R right) {
        return new ImmutablePair<L, R>(left, right);
    }


    public ImmutablePair(final L left, final R right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return left;
    }


    @Override
    public R getRight() {
        return right;
    }
    @Override
    public R setValue(final R value) {
        throw new EasyRetryCommonException("非法操作");

    }
}
