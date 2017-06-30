/*
 * Copyright 2017 Scopely, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scopely.adapper.utils;


import android.support.annotation.Nullable;

import java.util.Comparator;

public class CompareUtils {

    /**
     * Merges multiple Comparators into a single Comparator.
     * @param comparators A list of Comparators, in the order they will be given priority in the comparison.
     * @return a Comparator that merges the provided Comparators.
     * During a comparison, it will move through the provided Comparators
     * until it finds the first one that evaluates to a non-zero value.
     * If it exhausts all the provided Comparators, it will return zero for the whole comparison.
     */
    @SafeVarargs
    public static <T> Comparator<T> mergeComparators(final Comparator<? super T>... comparators){
        return new Comparator<T>() {
            @Override
            public int compare(T lhs, T rhs) {
                return multiDimensionalCompare(lhs, rhs, comparators);
            }
        };
    }

    /**
     * Compares two items using a provided list of Comparators.
     * Moves through the provided Comparators until it finds the first one that evaluates to a non-zero value.
     * If it exhausts all the provided Comparators, it will return zero for the whole comparison.
     */
    @SafeVarargs
    public static <T> int multiDimensionalCompare(T lhs, T rhs, Comparator<? super T>... comparators){
        for(Comparator<? super T> comparator : comparators){
            int result = comparator.compare(lhs, rhs);
            if(result != 0){
                return result;
            }
        }
        return 0;
    }

    /**
     * Takes a Comparator that is not null safe, and returns a version of the same Comparator that is
     * @param nullsLessThan true iff null values come before non-null values in the comparison, false otherwise
     */
    public static <T> Comparator<T> getNullSafeComparator(final Comparator<? super T> comparator, final boolean nullsLessThan) {
        return new Comparator<T>() {
            @Override
            public int compare(T lhs, T rhs) {
                return nullableCompare(lhs, rhs, comparator, nullsLessThan);
            }
        };
    }

    /**
     * Compares two items using the provided Comparator in a way that accepts nulls, even if the Comparator itself does not safely accept them
     * @param nullsLessThan true iff null values come before non-null values in the comparison, false otherwise
     */
    public static <T> int nullableCompare(@Nullable T lhs, @Nullable T rhs, Comparator<? super T> comparator, boolean nullsLessThan) {
        if(lhs != null && rhs != null) {
            return comparator.compare(lhs, rhs);
        } else if (lhs == null && rhs == null) {
            return 0;
        } else if (lhs == null) {
            return nullsLessThan ? -1 : 1;
        } else {
            return nullsLessThan ? 1 : -1;
        }
    }

    /**
     * Takes a Comparator that is not null safe, and returns a version of the same Comparator that is
     * @param nullsLessThan true iff null values come before non-null values in the comparison, false otherwise
     */
    public static <T extends Comparable<T>> Comparator<T> getNullSafeComparator(Class<T> clazz, final boolean nullsLessThan) {
        return new Comparator<T>() {
            @Override
            public int compare(T lhs, T rhs) {
                return nullableCompare(lhs, rhs, nullsLessThan);
            }
        };
    }

    /**
     * Compares two Comparable items in a null safe way
     * @param nullsLessThan true iff null values come before non-null values in the comparison, false otherwise
     */
    public static <T extends Comparable<T>> int nullableCompare(@Nullable T lhs, @Nullable T rhs, boolean nullsLessThan) {
        if(lhs != null && rhs != null) {
            return lhs.compareTo(rhs);
        } else if (lhs == null && rhs == null) {
            return 0;
        } else if (lhs == null) {
            return nullsLessThan ? -1 : 1;
        } else {
            return nullsLessThan ? 1 : -1;
        }
    }
}
