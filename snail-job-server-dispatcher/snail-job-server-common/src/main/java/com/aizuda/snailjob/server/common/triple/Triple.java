package com.aizuda.snailjob.server.common.triple;

import cn.hutool.core.builder.CompareToBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author: xiaowoniu
 * @date : 2023-11-24 08:54
 * @since : 2.5.0
 */
public abstract class Triple<L, M, R> implements Comparable<Triple<L, M, R>>, Serializable {

    private static final long serialVersionUID = 1L;

    public static <L, M, R> Triple<L, M, R> of(final L left, final M middle, final R right) {
        return new ImmutableTriple<>(left, middle, right);
    }

    public abstract L getLeft();

    public abstract M getMiddle();

    public abstract R getRight();

    @Override
    public int compareTo(final Triple<L, M, R> other) {
        return new CompareToBuilder().append(getLeft(), other.getLeft())
                .append(getMiddle(), other.getMiddle())
                .append(getRight(), other.getRight()).toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Triple<?, ?, ?>) {
            final Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
            return Objects.equals(getLeft(), other.getLeft())
                    && Objects.equals(getMiddle(), other.getMiddle())
                    && Objects.equals(getRight(), other.getRight());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getLeft() == null ? 0 : getLeft().hashCode()) ^
                (getMiddle() == null ? 0 : getMiddle().hashCode()) ^
                (getRight() == null ? 0 : getRight().hashCode());
    }

    @Override
    public String toString() {
        return "(" + getLeft() + "," + getMiddle() + "," + getRight() + ")";
    }


    public String toString(final String format) {
        return String.format(format, getLeft(), getMiddle(), getRight());
    }


}
