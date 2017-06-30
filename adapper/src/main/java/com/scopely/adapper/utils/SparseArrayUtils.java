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
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import java.util.Iterator;

/**
 * Util class for working with SparseArrays.
 * Contains methods for combining, iterating over, and determining range of keys.
 */
public class SparseArrayUtils {

    public static SparseBooleanArray combine(SparseBooleanArray... arrays) {
        SparseBooleanArray combined = new SparseBooleanArray();
        for(SparseBooleanArray array : arrays) {
            for(int i = 0; i < array.size(); i++) {
                combined.append(array.keyAt(i), array.valueAt(i));
            }
        }
        return combined;
    }

    public static SparseIntArray combine(SparseIntArray... arrays) {
        SparseIntArray combined = new SparseIntArray();
        for(SparseIntArray array : arrays) {
            for(int i = 0; i < array.size(); i++) {
                combined.append(array.keyAt(i), array.valueAt(i));
            }
        }
        return combined;
    }

    public static Iterable<Pair<Integer, Boolean>> iterable(final SparseBooleanArray array) {
        return new Iterable<Pair<Integer, Boolean>>() {
            @Override
            public Iterator<Pair<Integer, Boolean>> iterator() {
                return new Iterator<Pair<Integer, Boolean>>() {
                    int i = -1;

                    @Override
                    public boolean hasNext() {
                        return i + 1 < array.size();
                    }

                    @Override
                    public Pair<Integer, Boolean> next() {
                        i++;
                        return new Pair<Integer, Boolean>(array.keyAt(i), array.valueAt(i));
                    }

                    @Override
                    public void remove() {
                        array.delete(array.keyAt(i));
                    }
                };
            }
        };
    }

    public static Iterable<Pair<Integer, Integer>> iterable(final SparseIntArray array) {
        return new Iterable<Pair<Integer, Integer>>() {
            @Override
            public Iterator<Pair<Integer, Integer>> iterator() {
                return new Iterator<Pair<Integer, Integer>>() {
                    int i = -1;

                    @Override
                    public boolean hasNext() {
                        return i + 1 < array.size();
                    }

                    @Override
                    public Pair<Integer, Integer> next() {
                        i++;
                        return new Pair<Integer, Integer>(array.keyAt(i), array.valueAt(i));
                    }

                    @Override
                    public void remove() {
                        array.delete(array.keyAt(i));
                    }
                };
            }
        };
    }

    public static <T> Iterable<Pair<Integer, T>> iterable(final SparseArray<T> array) {
        return new Iterable<Pair<Integer, T>>() {
            @Override
            public Iterator<Pair<Integer, T>> iterator() {
                return new Iterator<Pair<Integer, T>>() {
                    int i = -1;

                    @Override
                    public boolean hasNext() {
                        return i + 1 < array.size();
                    }

                    @Override
                    public Pair<Integer, T> next() {
                        i++;
                        return new Pair<Integer, T>(array.keyAt(i), array.valueAt(i));
                    }

                    @Override
                    public void remove() {
                        array.delete(array.keyAt(i));
                    }
                };
            }
        };
    }

    /**
     * Calculates the start and end of a contiguous range of keys in a sparse array.
     *
     * @param array A {@link SparseBooleanArray SparseBooleanArray}
     * @return a {@link Pair Pair<Integer, Integer>} where the first element in the smallest key in {@param array} and the second element is the largest key in {@param array}. Returns null if the array is empty, or the array contains non-contiguous values.
     */
    @Nullable
    public static Pair<Integer, Integer> getRange(SparseBooleanArray array) {
        if(array.size() == 0) return null;
        if(array.size() == 1) return new Pair<>(array.keyAt(0), array.keyAt(0));
        int largest = 0;
        int smallest = 0;
        for(int i = 0; i < array.size(); i++){
            int val = array.keyAt(i);
            if(i > 0 && val - 1 != array.keyAt(i -1)) {
                return null;
            }
            if(val > largest) largest = val;
            if(val < smallest) smallest = val;
        }
        return new Pair<>(smallest, largest);
    }
}
