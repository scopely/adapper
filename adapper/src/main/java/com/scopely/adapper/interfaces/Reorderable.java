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

package com.scopely.adapper.interfaces;

import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

/**
 *
 * An interface to be implemented by Adapters to indicate that they keep track of insertions, deletions and reorderings between dataset changes.
 *
 */
public interface Reorderable {
    int NOT_PRESENT = -1;

    SparseBooleanArray getInsertions();

    SparseBooleanArray getDeletions();

    SparseIntArray getReorderings();
}
