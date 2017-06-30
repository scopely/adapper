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

package com.scopely.adapper.extras;

import android.support.v7.widget.GridLayoutManager;
import android.util.SparseIntArray;

/**
 * A SpanSizeLookup that describes width of elements not by how many spans they take up, but by how many of said element would fill a row.
 */
public abstract class InvertedSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    public final int total;
    public final SparseIntArray sparseIntArray;

    public InvertedSpanSizeLookup(int... possibleItemsPerRow) {
        sparseIntArray = new SparseIntArray(possibleItemsPerRow.length);
        total = lcm(possibleItemsPerRow);
        for(int i : possibleItemsPerRow) {
            sparseIntArray.put(i, total/i);
        }

    }

    @Override
    public int getSpanSize(int position) {
        return sparseIntArray.get(getItemsPerRow(position));
    }

    public abstract int getItemsPerRow(int position);

    private static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private static int gcd(int[] input) {
        int result = input[0];
        for(int i = 1; i < input.length; i++) result = gcd(result, input[i]);
        return result;
    }

    private static int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }

    private static int lcm(int[] input) {
        int result = input[0];
        for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
        return result;
    }

    public int getRequiredSpans() {
        return total;
    }
}
