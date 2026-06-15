package org.tcshare.poi;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;


/**
 * A pair consisting of two elements.
 * <p>
 * This class is an abstract implementation defining the basic API.
 * It refers to the elements as 'left' and 'right'. It also implements the
 * {@code Map.Entry} interface where the key is 'left' and the value is 'right'.
 * </p>
 * <p>
 * Subclass implementations may be mutable or immutable.
 * However, there is no restriction on the type of the stored objects that may be stored.
 * If mutable objects are stored in the pair, then the pair itself effectively becomes mutable.
 * </p>
 *
 * @param <L> the left element type
 * @param <R> the right element type
 * @since Lang 3.0
 */
public abstract class Pair<L, R> implements Map.Entry<L, R>, Comparable<Pair<L, R>>, Serializable {

    /** Serialization version */
    private static final long serialVersionUID = 4954918890077093841L;

    /**
     * An empty array.
     * <p>
     * Consider using {@link #emptyArray()} to avoid generics warnings.
     * </p>
     *
     * @since 3.10
     */
    public static final Pair<?, ?>[] EMPTY_ARRAY = {};

    /**
     * Returns the empty array singleton that can be assigned without compiler warning.
     *
     * @param <L> the left element type
     * @param <R> the right element type
     * @return an empty array of type Pair<L, R>
     * @since 3.10
     */
    @SuppressWarnings("unchecked")
    public static <L, R> Pair<L, R>[] emptyArray() {
        return (Pair<L, R>[]) EMPTY_ARRAY;
    }

    /**
     * Obtains an immutable pair of from two objects inferring the generic types.
     *
     * @param <L>   the left element type
     * @param <R>   the right element type
     * @param left  the left object, may be null
     * @param right the right object, may be null
     * @return an immutable Pair created from two elements
     * @see ImmutablePair#of(Object, Object)
     */
    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return ImmutablePair.of(left, right);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the left element of this pair.
     *
     * @return the left element
     */
    public abstract L getLeft();

    /**
     * Gets the right element of this pair.
     *
     * @return the right element
     */
    public abstract R getRight();

    /**
     * {@inheritDoc}
     */
    @Override
    public final L getKey() {
        return getLeft();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public R getValue() {
        return getRight();
    }

    //-----------------------------------------------------------------------
    /**
     * Compares the pair based on the left element followed by the right element.
     * The types must be {@link Comparable}.
     *
     * @param other the other pair
     * @return negative number, zero, or positive number if this is less than, equal
     *         to, or greater than {@code other}
     */
    @Override
    public int compareTo(final Pair<L, R> other) {
        if (this == other) {
            return 0;
        }

        // 比较 left
        if (getLeft() == other.getLeft()) {
            // 相等，继续比较
        } else if (getLeft() == null) {
            return -1;
        } else if (other.getLeft() == null) {
            return 1;
        } else {
            int cmp = ((Comparable<Object>) getLeft()).compareTo(other.getLeft());
            if (cmp != 0) {
                return cmp;
            }
        }

        // 比较 right
        if (getRight() == other.getRight()) {
            // 相等，继续比较
        } else if (getRight() == null) {
            return -1;
        } else if (other.getRight() == null) {
            return 1;
        } else {
            return ((Comparable<Object>) getRight()).compareTo(other.getRight());
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Map.Entry<?, ?>)) {
            return false;
        }
        final Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
        return Objects.equals(getKey(), other.getKey())
                && Objects.equals(getValue(), other.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return '(' + getLeft().toString() + ',' + getRight().toString() + ')';
    }

    /**
     * Formats the string as {@code ($left,$right)}.
     *
     * @param format the string to format (two placeholders)
     * @return the formatted string
     */
    public String toString(final String format) {
        return String.format(format, getLeft(), getRight());
    }
}