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

import android.os.Build;
import android.util.ArraySet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Util class for generating {@link Set Set} instances.
 * Uses ArraySet on versions of Android M and up, falls back to HashSet for older version.
 */
public class SetUtils {
    public static <T> Set<T> newSet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ArraySet<>();
        } else {
            return new HashSet<>();
        }
    }

    public static <T> Set<T> newSet(int capacity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ArraySet<>(capacity);
        } else {
            return new HashSet<>(capacity);
        }
    }

    @SafeVarargs
    public static <T> Set<T> initSet(T... elements) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Set<T> set = new ArraySet<>(elements.length);
            Collections.addAll(set, elements);
            return set;
        } else {
            return new HashSet<>(Arrays.asList(elements));
        }
    }

    public static <T> Set<T> newSet(Collection<? extends T> collection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Set<T> set = new ArraySet<>(collection.size());
            set.addAll(collection);
            return set;
        } else {
            return new HashSet<>(collection);
        }
    }
}
