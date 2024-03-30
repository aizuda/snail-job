package com.aizuda.easy.retry.server.common.triple;

import cn.hutool.core.builder.CompareToBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author: xiaowoniu
 * @date : 2024-03-30
 * @since : 3.2.0
 */
public abstract class Pair<L, R>  implements Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new ImmutablePair<>(left, right);
    }

    public abstract L getLeft();


    public abstract R getRight();


    @Override
    public final L getKey() {
        return getLeft();
    }


    @Override
    public R getValue() {
        return getRight();
    }

    @Override
    public int compareTo(final Pair<L, R> other) {
        return new CompareToBuilder().append(getLeft(), other.getLeft())
            .append(getRight(), other.getRight()).toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Map.Entry<?, ?>) {
            final Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
            return Objects.equals(getKey(), other.getKey())
                && Objects.equals(getValue(), other.getValue());
        }
        return false;
    }

    /**
     * <p>Returns a suitable hash code.
     * The hash code follows the definition in {@code Map.Entry}.</p>
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return (getKey() == null ? 0 : getKey().hashCode()) ^
            (getValue() == null ? 0 : getValue().hashCode());
    }

    /**
     * <p>Returns a String representation of this pair using the format {@code ($left,$right)}.</p>
     *
     * @return a string describing this object, not null
     */
    @Override
    public String toString() {
        return "(" + getLeft() + ',' + getRight() + ')';
    }

    public String toString(final String format) {
        return String.format(format, getLeft(), getRight());
    }
}
