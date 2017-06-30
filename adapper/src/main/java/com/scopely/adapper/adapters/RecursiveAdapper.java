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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.scopely.adapper.interfaces.Bidentifier;
import com.scopely.adapper.interfaces.GroupPositionIdentifier;
import com.scopely.adapper.interfaces.Reorderable;
import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.utils.CompositeFilter;
import com.scopely.adapper.utils.SparseArrayUtils;
import com.scopely.adapper.utils.SetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An Adapper that wraps several other Adappers, effectively stacking them in order.
 * Useful for creating an Adapper backed by several independent lists or datasets.
 */
public class RecursiveAdapper<Model> extends BaseAdapper<Model, RecyclerView.ViewHolder> implements Filterable, Reorderable, GroupPositionIdentifier {
    protected final List<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>> adapters = new ArrayList<>();
    final Set<Integer> viewTypes = SetUtils.newSet();
    private Map<BaseAdapper, Integer> countAtLastUpdate = new HashMap<>();

    @SafeVarargs
    public RecursiveAdapper(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>... adapters) {
        setBidentifier(new RecursiveIdentifier<>(this));
        Collections.addAll(this.adapters, adapters);
        setViewTypes(this.adapters);
    }

    @SuppressWarnings("UnusedDeclaration")
    public RecursiveAdapper(List<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>> adapters) {
        setBidentifier(new RecursiveIdentifier<>(this));
        this.adapters.addAll(adapters);
        setViewTypes(this.adapters);
    }

    @SuppressWarnings("UnusedDeclaration")
    public RecursiveAdapper<Model> addAdapter(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter) {
        adapters.add(adapter);
        setViewTypes(adapters);
        return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public RecursiveAdapper<Model> addAdapter(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter, int index) {
        adapters.add(index, adapter);
        setViewTypes(adapters);
        return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void removeAdapter(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter) {
        adapters.remove(adapter);
        setViewTypes(adapters);
    }

    private void setViewTypes(List<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>> adapters) {
        viewTypes.clear();
        for(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapper : adapters) {
            viewTypes.addAll(adapper.getViewTypes());
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            count += adapter.getItemCount();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer> pair = getInternalAdapter(position);
        return pair.first.getItem(pair.second);
    }

    @Override
    public long getItemId(int position) {
        try {
            Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer> pair = getInternalAdapter(position);
            return pair.first.getItemId(pair.second);
        } catch (IndexOutOfBoundsException e) {
            return RecyclerView.NO_ID;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapper = getInternalAdapterForViewType(viewType);
        return adapper.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer> pair = getInternalAdapter(position);
        pair.first.onBindViewHolderCast(holder, pair.second);
    }

    private BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> getInternalAdapterForViewType(int viewType) {
        for(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapper : adapters) {
            if(adapper.getViewTypes().contains(viewType)) {
                return adapper;
            }
        }
        return null;
    }

    private int getSuperIndex(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> src, int position) {
        int count = position;
        for (BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            if (!src.equals(adapter)) {
                count += adapter.getItemCount();
            } else {
                break;
            }
        }
        return count;
    }

    private int getSuperIndexAtLastUpdate(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> src, int position) {
        int count = position;
        for (BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            if (!src.equals(adapter)) {
                count += getCountAtLastUpdate(adapter);
            } else {
                break;
            }
        }
        return count;
    }

    private int getCountAtLastUpdate(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter) {
        Integer integer = countAtLastUpdate.get(adapter);
        return integer != null ? integer : 0;
    }

    public Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer> getInternalAdapter (int position) {
        for (BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            if (position >= adapter.getItemCount()) {
                position -= adapter.getItemCount();
            } else {
                return new Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer>(adapter, position);
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public Set<Integer> getViewTypes() {
        return viewTypes;
    }

    @Override
    public int getItemViewType(int position) {
        Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer> pair = getInternalAdapter(position);
        return pair.first.getItemViewType(pair.second);
    }

    @Override
    public boolean isModel(int position) {
        Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer> pair = getInternalAdapter(position);
        return pair.first.isModel(pair.second);
    }

    private void addLayout(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter) {
        Set<Integer> viewTypes = adapter.getViewTypes();
        if(viewTypes != null){
            this.viewTypes.addAll(viewTypes);
        }
        countAtLastUpdate.put(adapter, adapter.getItemCount());
    }

    @Override
    protected void onChanged() {
        for (BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            adapter.notifyDataSetChanged();
            countAtLastUpdate.put(adapter, adapter.getItemCount());
        }
    }

    @Override
    public Filter getFilter() {
        Filter[] filters = new Filter[adapters.size()];
        for(int i = 0; i < adapters.size(); i++) {
            BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter = adapters.get(i);
            if(adapter instanceof Filterable){
                filters[i] = ((Filterable) adapter).getFilter();
            }
        }

        return new CompositeFilter(filters){
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                super.publishResults(constraint, results);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public SparseBooleanArray getInsertions() {
        SparseBooleanArray list = new SparseBooleanArray();
        for(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            if(adapter instanceof Reorderable) {
                int offset = getSuperIndex(adapter, 0);
                SparseBooleanArray innerArray = ((Reorderable)adapter).getInsertions();
                for (int i = 0; i < innerArray.size(); i++) {
                    int index = innerArray.keyAt(i);
                    list.put(index + offset, true);
                }
            }
        }
        return list;
    }

    @Override
    public SparseBooleanArray getDeletions() {
        SparseBooleanArray list = new SparseBooleanArray();
        for(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            if(adapter instanceof Reorderable) {
                int offset = getSuperIndexAtLastUpdate(adapter, 0);
                SparseBooleanArray innerArray = ((Reorderable)adapter).getDeletions();
                for (int i = 0; i < innerArray.size(); i++) {
                    int index = innerArray.keyAt(i);
                    list.put(index + offset, true);
                }
            }
        }
        return list;
    }

    @Override
    public SparseIntArray getReorderings() {
        SparseIntArray transitions = new SparseIntArray();
        for(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapter : adapters) {
            if(adapter instanceof Reorderable) {
                int offset = getSuperIndex(adapter, 0);
                int oldOffset = getSuperIndexAtLastUpdate(adapter, 0);
                SparseIntArray innerArray = ((Reorderable)adapter).getReorderings();
                for (int i = 0; i < innerArray.size(); i++) {
                    int index = innerArray.keyAt(i);
                    int oldIndex = innerArray.valueAt(i);
                    transitions.put(index + offset, oldIndex + oldOffset);
                }
            }
        }
        return transitions;
    }

    @Override
    public boolean isGroup(int position) {
        Pair<BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder>, Integer> pair = getInternalAdapter(position);
        return pair.first instanceof GroupPositionIdentifier && ((GroupPositionIdentifier) pair.first).isGroup(pair.second);
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

    @Override
    public RecursiveAdapper<Model> setSelectionManager(@Nullable SelectionManager<? extends Model> selectionManager) {
        for(BaseAdapper<? extends Model, ? extends RecyclerView.ViewHolder> adapper : adapters) {
            setSelectionManager(adapper, selectionManager);
        }
        super.setSelectionManager(selectionManager);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T extends Model> void setSelectionManager(BaseAdapper<T, ? extends RecyclerView.ViewHolder> adapper, SelectionManager<? extends Model> selectionManager) {
        adapper.setSelectionManager(new RecursiveAdapperSelectionManager<T>(this, adapper, (SelectionManager<? extends T>) selectionManager));
    }

    private static class RecursiveAdapperSelectionManager<T> extends ConversionSelectManager<T> {
        private final RecursiveAdapper<? super T> parent;
        private final BaseAdapper<T, ? extends RecyclerView.ViewHolder> child;

        public RecursiveAdapperSelectionManager(RecursiveAdapper<? super T> parent, BaseAdapper<T, ? extends RecyclerView.ViewHolder> child, SelectionManager<? extends T> delegate) {
            super(delegate);
            this.parent = parent;
            this.child = child;
        }

        @Override
        protected int convertPosition(int position) {
            return parent.getSuperIndexAtLastUpdate(child, position);
        }
    }

    private static class RecursiveIdentifier<T> implements Bidentifier<T> {

        private final RecursiveAdapper<T> parent;

        public RecursiveIdentifier(RecursiveAdapper<T> parent) {
            this.parent = parent;
        }

        @Override
        public long getId(int position) {
            try {
                Pair<BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder>, Integer> pair = parent.getInternalAdapter(position);
                return pair.first.getItemId(pair.second);
            } catch (IndexOutOfBoundsException e) {
                return RecyclerView.NO_ID;
            }
        }

        @Override
        public Set<? extends T> getModels(Set<Long> ids) {
            Set<T> set = SetUtils.newSet();
            for(BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper : parent.adapters) {
                set.addAll(adapper.getItems(ids));
            }
            return set;
        }
    }
}
