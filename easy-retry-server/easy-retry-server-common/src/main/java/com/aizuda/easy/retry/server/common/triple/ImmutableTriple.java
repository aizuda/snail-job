package com.aizuda.easy.retry.server.common.triple;

import java.io.Serial;

/**
 * @author: xiaowoniu
 * @date : 2023-11-24 08:56
 * @since : 2.5.0
 */
public final class ImmutableTriple<L, M, R> extends Triple<L, M, R> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final L left;
    private final M middle;
    private final R right;

    public static <L, M, R> ImmutableTriple<L, M, R> of(final L left, final M middle, final R right) {
        return new ImmutableTriple<>(left, middle, right);
    }

    public ImmutableTriple(final L left, final M middle, final R right) {
        super();
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return left;
    }

    @Override
    public M getMiddle() {
        return middle;
    }

    @Override
    public R getRight() {
        return right;
    }
}
