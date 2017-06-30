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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.scopely.adapper.impls.HashCodeIdentifier;
import com.scopely.adapper.impls.BidentifierImpl;
import com.scopely.adapper.impls.NaiveLookup;
import com.scopely.adapper.impls.TypedViewHolder;
import com.scopely.adapper.interfaces.FilterFunction;
import com.scopely.adapper.interfaces.Reorderable;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.utils.ListUtils;
import com.scopely.adapper.utils.SparseArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An Adapper that displays a {@link List} of items
 * @param <Model> The class of the items in the list
 * @param <GenericView> The {@link View} class used to display the items in the list
 */
public class ListAdapper<Model, GenericView extends View> extends BaseAdapper<Model, TypedViewHolder<? super Model, ? extends GenericView>> implements Filterable, Reorderable {

    protected List<? extends Model> source;
    protected List<? extends Model> list;
    protected List<? extends Model> visibleList;

    protected final ViewProvider<? super Model, ? extends GenericView> provider;
    @Nullable
    private FilterFunction<? super Model> filterFunction;
    @Nullable
    public CharSequence constraint;

    public ListAdapper(List<? extends Model> source, ViewProvider<? super Model, ? extends GenericView> provider) {
        this.source = source;
        this.provider = provider;
        this.list = new ArrayList<>(source);
        this.visibleList = list;
        setBidentifier(new BidentifierImpl<>(new HashCodeIdentifier(this), new NaiveLookup<>(this)));
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(TypedViewHolder<? super Model, ? extends GenericView> holder, int position) {
        holder.bind(getModel(position), position, getSelectionManager(position));
    }


    @NonNull
    @Override
    public Set<Integer> getViewTypes() {
        return provider.getViewTypes();
    }

    @Override
    public Model getModel(int position) {
        return (Model) getItem(position);
    }

    @Override
    public int getItemViewType(int position) {
        return provider.getViewType(getModel(position));
    }


    @Override
    public boolean isModel(int position) {
        return true;
    }

    @Override
    public Object getItem(int position) {
        return position < visibleList.size() ? visibleList.get(position) : null;
    }

    @Override
    public TypedViewHolder<? super Model, ? extends GenericView> onCreateViewHolder(ViewGroup parent, int viewType) {
        return provider.create(LayoutInflater.from(parent.getContext()), parent, viewType);
    }

    @Override
    public int getItemCount() {
        return visibleList.size();
    }

    public ListAdapper<Model, GenericView> setFilterFunction(@Nullable FilterFunction<? super Model> filterFunction) {
        this.filterFunction = filterFunction;
        return this;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        public FilterResults performFiltering(CharSequence constraint) {
            ListAdapper.this.constraint = constraint;
            FilterResults results = new FilterResults();
            if (filterFunction == null) {
                results.values = list;
                results.count = list.size();
            } else {
                List<Model> filteredList = new ArrayList<>();
                for (Model model : list) {
                    if (filterFunction.filter(model, constraint)) {
                        filteredList.add(model);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
            }
            return results;
        }

        @Override
        public void publishResults(CharSequence constraint, FilterResults results) {
            if (results == null) {
                notifyDataSetChanged();
            } else {
                visibleList = (List<? extends Model>) results.values;
                ListAdapper.super.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onChanged() {
        list = new ArrayList<>(source);
        if(constraint == null || constraint.length() == 0){
            visibleList = list;
        }
    }

    @Override
    public SparseBooleanArray getInsertions() {
        List<? extends Model> oldList = visibleList;
        List<? extends Model> newList = getNextVisibleList();

        return ListUtils.getInsertions(oldList, newList);
    }

    @Override
    public SparseBooleanArray getDeletions() {
        List<? extends Model> oldList = visibleList;
        List<? extends Model> newList = getNextVisibleList();

        return ListUtils.getDeletions(oldList, newList);
    }

    @Override
    public SparseIntArray getReorderings() {
        List<? extends Model> oldList = visibleList;
        List<? extends Model> newList = getNextVisibleList();

        return ListUtils.getReorderings(oldList, newList);
    }

    public List<? extends Model> getVisibleList() {
        return visibleList;
    }

    public List<? extends Model> getNextVisibleList() {
        if(constraint == null || constraint.length() == 0){
            return new ArrayList<>(source);
        } else {
            return visibleList;
        }
    }

    @Override
    public void update() {
        SparseBooleanArray deletions = getDeletions();
        if(deletions.size() > 0) {
            Pair<Integer, Integer> startEnd = SparseArrayUtils.getRange(deletions);
            if(startEnd != null){
                notifyItemRangeRemoved(startEnd.first, startEnd.second - startEnd.first + 1);
            } else {
                notifyDataSetChanged();
                return;
            }
        }
        SparseBooleanArray insertions = getInsertions();
        if(insertions.size() > 0) {
            Pair<Integer, Integer> startEnd = SparseArrayUtils.getRange(insertions);
            if(startEnd != null){
                notifyItemRangeInserted(startEnd.first, startEnd.second - startEnd.first + 1);
            } else {
                notifyDataSetChanged();
                return;
            }
        }
        for (Pair<Integer, Integer> pair : SparseArrayUtils.iterable(getReorderings())) {
            notifyItemMoved(pair.first, pair.second);
        }
    }
}
