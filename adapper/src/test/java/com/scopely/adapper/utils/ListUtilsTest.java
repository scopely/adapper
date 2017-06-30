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

import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ListUtilsTest {
    private static final List<Integer> list05 = Arrays.asList(0,1,2,3,4,5);
    private static final List<Integer> list15 = Arrays.asList(1,2,3,4,5);
    private static final List<Integer> list06 = Arrays.asList(0,1,2,3,4,5,6);

    private static final List<Integer> listFlip = Arrays.asList(0,1,2,3,5,4);
    private static final List<Integer> listGap = Arrays.asList(5,0,1,2,3,4);
    private static final List<Integer> listMultiReorder = Arrays.asList(5,0,4,1,2,3);
    private static final List<Integer> listSwap = Arrays.asList(5,1,2,3,4,0);



    @Test
    public void testSingleInsert() {
        SparseBooleanArray insertions = ListUtils.getInsertions(list05, list06);
        assertThat(insertions.size(), is(1));
        assertThat(insertions.get(6), is(true));
    }

    @Test
    public void testSingleInsertHead() {
        SparseBooleanArray insertions = ListUtils.getInsertions(list15, list05);
        assertThat(insertions.size(), is(1));
        assertThat(insertions.get(0), is(true));
    }

    @Test
    public void testMultiInsert() {
        SparseBooleanArray insertions = ListUtils.getInsertions(list15, list06);
        assertThat(insertions.size(), is(2));
        assertThat(insertions.get(6), is(true));
        assertThat(insertions.get(0), is(true));
    }

    @Test
    public void testSingleDeletion() {
        SparseBooleanArray deletions = ListUtils.getDeletions(list06, list05);
        assertThat(deletions.size(), is(1));
        assertThat(deletions.get(6), is(true));
    }

    @Test
    public void testSingleDeletionHead() {
        SparseBooleanArray deletions = ListUtils.getDeletions(list05, list15);
        assertThat(deletions.size(), is(1));
        assertThat(deletions.get(0), is(true));
    }

    @Test
    public void testMultiDelete() {
        SparseBooleanArray deletions = ListUtils.getDeletions(list06, list15);
        assertThat(deletions.size(), is(2));
        assertThat(deletions.get(6), is(true));
        assertThat(deletions.get(0), is(true));
    }

    @Test
    public void testSingleReorderDown() {
        SparseIntArray reorderings = ListUtils.getReorderings(list05, listFlip);
        assertThat(reorderings.size(), is(1));
        assertThat(reorderings.get(4), is(5));
    }

    @Test
    public void testSingleReorderGapDown() {
        SparseIntArray reorderings = ListUtils.getReorderings(list05, listGap);
        assertThat(reorderings.size(), is(1));
        assertThat(reorderings.get(0), is(5));
    }

    @Test
    public void testMultiReorderDown() {
        SparseIntArray reorderings = ListUtils.getReorderings(list05, listMultiReorder);
        assertThat(reorderings.size(), is(2));
        assertThat(reorderings.get(0), is(5));
        assertThat(reorderings.get(2), is(4));
    }

    @Test
    public void testSingleReorderUp() {
        SparseIntArray reorderings = ListUtils.getReorderings(listFlip, list05);
        assertThat(reorderings.size(), is(1));
        assertThat(reorderings.get(4), is(5));
    }

    @Test
    public void testSingleReorderGapUp() {
        SparseIntArray reorderings = ListUtils.getReorderings(listGap, list05);
        assertThat(reorderings.size(), is(1));
        assertThat(reorderings.get(5), is(0));
    }

    @Test
    public void testMultiReorderUp() {
        SparseIntArray reorderings = ListUtils.getReorderings(listMultiReorder, list05);
        assertThat(reorderings.size(), is(2));
        assertThat(reorderings.get(5), is(0));
        assertThat(reorderings.get(4), is(2));
    }

    @Test
    public void testReorderSwap() {
        SparseIntArray reorderings = ListUtils.getReorderings(list05, listSwap);
        assertThat(reorderings.size(), is(2));
        assertThat(reorderings.get(5), is(0));
        assertThat(reorderings.get(0), is(5));
    }
}
