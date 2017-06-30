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

import java.util.Comparator;

/**
 * An augmented {@link java.util.Comparator} that identifies a containing <Group> for each item in the list, and then sorts the list based on the <Group> each item belongs to.
 * @param <Model> The type of the objects in the list being sorted and grouped
 * @param <Group> The type of the groups each object belongs to
 */
public interface GroupComparator<Model, Group> extends Comparator<Model> {
    Group getGroup(Model item);
    Comparator<Model> getIntraGroupComparator();
    Comparator<Group> getGroupComparator();
}
