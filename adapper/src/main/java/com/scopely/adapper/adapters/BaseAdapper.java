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

import com.scopely.adapper.interfaces.Bidentifier;
import com.scopely.adapper.interfaces.SelectionManager;

import java.util.Collections;
import java.util.Set;

public abstract class BaseAdapper<Model, Holder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<Holder> {
    @Nullable
    private SelectionManager<? extends Model> selectionManager;
    @Nullable
    private Bidentifier<? extends Model> bidentifier;

    public BaseAdapper() {
        super();
        registerAdapterDataObserver(adapterDatasetObserver);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        //Unregistering and reregistering the adapterDatasetObserver ensures that it is the last DatasetObserver in the list,
        // and thus gets called first when the list is iterated over in reverse order.
        unregisterAdapterDataObserver(adapterDatasetObserver);
        registerAdapterDataObserver(adapterDatasetObserver);
    }

    protected abstract Set<Integer> getViewTypes();
    public abstract Object getItem(int position);
    public abstract boolean isModel(int position);

    @Nullable
    @SuppressWarnings("unchecked")
    public Model getModel(int position) {
        return isModel(position) ? (Model) getItem(position) : null;
    }

    @SuppressWarnings("unchecked")
    public void onBindViewHolderCast(RecyclerView.ViewHolder holder, int position) {
        onBindViewHolder((Holder) holder, position);
    }

    /**
     * In older versions of Adapper, several of the Adappers overrode {@link #notifyDataSetChanged()} in order to do some processing on dataset changes.
     * {@link android.support.v7.widget.RecyclerView.Adapter} has made that method final, so we hook in here to get the appropriate callbacks.
     */
    private final RecyclerView.AdapterDataObserver adapterDatasetObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            BaseAdapper.this.onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            BaseAdapper.this.onItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            BaseAdapper.this.onItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            BaseAdapper.this.onItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            BaseAdapper.this.onItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            BaseAdapper.this.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    protected void onItemRangeChanged(int positionStart, int itemCount, Object payload) {}

    protected void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        onChanged();
    }

    protected void onItemRangeRemoved(int positionStart, int itemCount) {
        onChanged();
    }

    protected void onItemRangeInserted(int positionStart, int itemCount) {
        onChanged();
    }

    protected void onItemRangeChanged(int positionStart, int itemCount) {}

    protected void onChanged() {}

    /**
     * The update() method defaults to a simple delegation to {@link #notifyDataSetChanged()},
     * but in subclasses that implement {@link com.scopely.adapper.interfaces.Reorderable}
     * it often computes position changes and delegates to the appropriate notify method:
     * {@link #notifyItemInserted(int)}, {@link #notifyItemRemoved(int)}, etc
     */
    public void update() {
        notifyDataSetChanged();
    }

    @Nullable
    protected SelectionManager getSelectionManager(int position) {
        return selectionManager;
    }

    public BaseAdapper<Model, Holder> setSelectionManager(@Nullable SelectionManager<? extends Model> selectionManager) {
        this.selectionManager = selectionManager;
        return this;
    }

    public BaseAdapper<Model, Holder> setBidentifier(@Nullable Bidentifier<? extends Model> bidentifier) {
        this.bidentifier = bidentifier;
        unregisterAdapterDataObserver(adapterDatasetObserver);
        if(!hasObservers()) {
            setHasStableIds(bidentifier != null);
        }
        registerAdapterDataObserver(adapterDatasetObserver);
        return this;
    }

    @SuppressWarnings("unchecked")
    public Set<? extends Model> getItems(Set<Long> ids) {
        return bidentifier != null ? bidentifier.getModels(ids) : Collections.EMPTY_SET;
    }

    @Override
    public long getItemId(int position) {
        return bidentifier != null ? bidentifier.getId(position) : super.getItemId(position);
    }

    @SuppressWarnings("unchecked")
    public Set<? extends Model> getSelections() {
        return selectionManager != null ? selectionManager.getSelections() : Collections.EMPTY_SET;
    }

    public void clearSelections() {
        if (selectionManager != null) {
            selectionManager.clearSelections();
        }
    }

    /**
     * A {@link SelectionManager} that delegates to an existing SelectionManager, but passes any inputted position through a conversion function before delegating it to the child SelectionManager.
     * Generally useful in Adappers that are composed of other Adappers, such as {@link RecursiveAdapper} or {@link GroupableAdapper}
     */
    protected abstract static class ConversionSelectManager<T> implements SelectionManager<T> {
        private final SelectionManager<? extends T> selectionManager;

        protected ConversionSelectManager(SelectionManager<? extends T> selectionManager) {
            this.selectionManager = selectionManager;
        }

        @Override
        public boolean selectItem(int position, boolean selected) {
            return selectionManager.selectItem(convertPosition(position), selected);
        }

        protected abstract int convertPosition(int position);

        @Override
        public boolean isItemSelected(int position) {
            return selectionManager.isItemSelected(convertPosition(position));
        }

        @Override
        public void clearSelections() {
            selectionManager.clearSelections();
        }

        @Override
        public Set<? extends T> getSelections() {
            return selectionManager.getSelections();
        }
    }
}
