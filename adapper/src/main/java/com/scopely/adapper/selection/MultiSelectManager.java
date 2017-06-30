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

package com.scopely.adapper.selection;

import android.support.v7.widget.RecyclerView;

import com.scopely.adapper.adapters.BaseAdapper;
import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.utils.SetUtils;

import java.util.Set;

/**
 * An implementation of SelectionManager that allows multiple items to be selected at a time.
 * Selecting a new item will add it to the set of selections, up to a defined maximum number of selected items.
 */
public class MultiSelectManager<T> implements SelectionManager<T> {
    private final Set<Long> selected; //Set of IDs of selected items
    private final BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper;
    private final int maximumSelectable;

    public MultiSelectManager(BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper) {
        this(adapper, Integer.MAX_VALUE);
    }

    public MultiSelectManager(BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper, int maximumSelectable) {
        this.maximumSelectable = maximumSelectable;
        selected = SetUtils.newSet();
        this.adapper = adapper;
        if(!adapper.hasStableIds()) {
            throw new RuntimeException("You cannot use SelectionManager with an Adapper that does not have stable ids");
        }
    }

    @Override
    public boolean selectItem(int position, boolean selected) {
        if(selected) {
            if(this.selected.size() == maximumSelectable) {
                onMaximumExceeded(maximumSelectable);
                return false;
            }
            this.selected.add(adapper.getItemId(position));
        } else {
            this.selected.remove(adapper.getItemId(position));
        }
        adapper.notifyItemChanged(position);
        return true;
    }

    protected void onMaximumExceeded(int maximumSelectable) {
        //Override in order to process events when the user attempts to select more than the maximum number allowed.
    }

    @Override
    public boolean isItemSelected(int position) {
        return selected.contains(adapper.getItemId(position));
    }

    @Override
    public void clearSelections() {
        selected.clear();
        adapper.notifyDataSetChanged();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<? extends T> getSelections() {
        return adapper.getItems(selected);
    }

    public int getCount() {
        return selected.size();
    }
}
