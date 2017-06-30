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

import java.util.Set;

/**
 * A selection manager tracks selections made within a list.
 * Its primary use is by Adappers to enable selection behavior within the RecyclerView
 * @param <T> the type of objects backing the list
 */
public interface SelectionManager<T> {
    /**
     * User has indicated they wish to select or unselect the item at {@param position}
     * @return true if selection was accepted
     */
    boolean selectItem(int position, boolean selected);

    /**
     * @return true iff the item at {@param position} has been marked as selected
     */
    boolean isItemSelected(int position);

    /**
     * @return the Set of items within the list that have been marked as selected
     */
    Set<? extends T> getSelections();

    void clearSelections();
}
