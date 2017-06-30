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

package com.scopely.adapper.impls;

import com.scopely.adapper.interfaces.GroupComparator;

import java.util.Comparator;

public abstract class GroupComparatorImpl<Model, Group> implements GroupComparator<Model, Group> {

    private final Comparator<Model> intraGroupComparator = new Comparator<Model>() {
        @Override
        public int compare(Model lhs, Model rhs) {
            return itemCompare(lhs, rhs);
        }
    };
    private final Comparator<Group> groupComparator = new Comparator<Group>() {
        @Override
        public int compare(Group lhs, Group rhs) {
            return groupCompare(lhs, rhs);
        }
    };

    protected abstract int groupCompare(Group lhs, Group rhs);

    protected abstract int itemCompare(Model lhs, Model rhs);

    @Override
    public Comparator<Model> getIntraGroupComparator() {
        return intraGroupComparator;
    }

    @Override
    public Comparator<Group> getGroupComparator() {
        return groupComparator;
    }

    @Override
    public int compare(Model lhs, Model rhs) {
        int categoryResult = groupCompare(getGroup(lhs), getGroup(rhs));
        return categoryResult != 0 ? categoryResult : itemCompare(lhs, rhs);
    }
}
