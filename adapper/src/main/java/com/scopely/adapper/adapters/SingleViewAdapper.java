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

package com.scopely.adapper.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.android.internal.util.Predicate;
import com.scopely.adapper.utils.SetUtils;

import java.util.Set;

/**
 * An Adapper that wraps a single {@link View} in the trappings of a {@link BaseAdapper}.
 * Generally used in order to include a {@link View} within a {@link RecursiveAdapper}
 */
public class SingleViewAdapper<Model> extends BaseAdapper<Model, RecyclerView.ViewHolder> implements Filterable {

    private final View view;
    private final int viewType;
    private final Set<Integer> viewTypes;
    private boolean visible = true;
    @Nullable Predicate<CharSequence> filterFunction;

    public SingleViewAdapper(View view) {
        this(view, null);
    }

    public SingleViewAdapper(View view, @Nullable @LayoutRes Integer viewType) {
        this.view = view;
        this.viewType = viewType != null ? viewType : view.hashCode();
        viewTypes = SetUtils.initSet(this.viewType);
    }

    @Override
    protected Set<Integer> getViewTypes() {
        return viewTypes;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public boolean isModel(int position) {
        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //no op;
    }

    @Override
    public int getItemCount() {
        return visible ? 1 : 0;
    }

    private final Filter filter = new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (filterFunction == null) {
                    results.values = true;
                    results.count = 1;
                } else {
                    boolean visible = filterFunction.apply(constraint);
                    results.values = visible;
                    results.count = visible ? 1 : 0;
                }
                return results;
            }

            @Override
            public void publishResults(CharSequence constraint, FilterResults results) {
                if (results == null) {
                    notifyDataSetChanged();
                } else {
                    visible = (boolean) results.values;
                    notifyDataSetChanged();
                }
            }
    };

    public void setFilterFunction(@Nullable Predicate<CharSequence> filterFunction) {
        this.filterFunction = filterFunction;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}