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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.scopely.adapper.adapters.BaseAdapper;
import com.scopely.adapper.interfaces.SelectionManager;

import java.util.Collections;
import java.util.Set;

/**
 * An implementation of SelectionManager that allows a single item to be selected at a time.
 * Selecting a new item will clear the existing selection.
 */
public class RadioSelectManager<T> implements SelectionManager {
    @Nullable
    private Long selected; //the ID of the selected item, as returned by adapper.getItemId(position)
    private final BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper;

    public RadioSelectManager(BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper) {
        this.adapper = adapper;
        if(!adapper.hasStableIds()) {
            throw new RuntimeException("You cannot use SelectionManager with an Adapper that does not have stable ids");
        }
    }

    @Override
    public boolean selectItem(int position, boolean selected) {
        this.selected = selected ? adapper.getItemId(position) : null;
        adapper.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean isItemSelected(int position) {
        return selected != null && selected == adapper.getItemId(position);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<? extends T> getSelections() {
        return adapper.getItems(Collections.singleton(selected));
    }

    @Override
    public void clearSelections() {
        selected = null;
        adapper.notifyDataSetChanged();
    }
}
