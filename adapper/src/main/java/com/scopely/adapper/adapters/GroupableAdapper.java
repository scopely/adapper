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
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.scopely.adapper.interfaces.Bidentifier;
import com.scopely.adapper.interfaces.FilterFunction;
import com.scopely.adapper.interfaces.GroupComparator;
import com.scopely.adapper.interfaces.GroupPositionIdentifier;
import com.scopely.adapper.interfaces.Reorderable;
import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.utils.CompositeFilter;
import com.scopely.adapper.utils.FunctionList;
import com.scopely.adapper.utils.SparseArrayUtils;
import com.scopely.adapper.utils.SetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An Adapper that sorts a {@link List} into groups and inserts a header {@link View} before each group
 * @param <Model> The class of the items in the list
 * @param <ModelView> The {@link View} class used to display the items in the list
 * @param <Category> The class of the item by which the list is grouped (often {@link String} or an enum)
 * @param <CategoryView> The {@link View} class used to display the group headers
 */
public class GroupableAdapper<Model, ModelView extends View, Category, CategoryView extends View> extends BaseAdapper<Model, RecyclerView.ViewHolder> implements Filterable, Reorderable, GroupPositionIdentifier{

    private final GroupComparator<Model, Category> comparator;
    private final List<? extends Model> source;
    private final ListAdapper<Model, ModelView> internalListAdapter;
    private final ListAdapper<Category, CategoryView> internalCategoryAdapter;
    private final Set<Integer> layouts;
    private List<Integer> categoryPositions;

    public GroupableAdapper(final List<? extends Model> list,
                            ViewProvider<? super Model, ? extends ModelView> provider,
                            final GroupComparator<Model, Category> comparator,
                            ViewProvider<Category, CategoryView> categoryProvider) {
        setBidentifier(new GroupableIdentifier<>(this));
        this.comparator = comparator;
        this.source = list;

        this.internalListAdapter = new ListAdapper<>(list, provider);

        final FunctionList.Function<Integer, Category> function = new FunctionList.Function<Integer, Category>() {
            @Override
            public Category evaluate(Integer input) {
                return comparator.getGroup(internalListAdapter.getModel(input));
            }
        };
        List<Category> categories = new FunctionList<Integer, Category>(null, function){
            @Override
            protected List<Integer> getList() {
                return generateCategoryPositions(internalListAdapter.getNextVisibleList(), comparator);
            }
        };
        categoryPositions = generateCategoryPositions(internalListAdapter.getVisibleList(), comparator);
        this.internalCategoryAdapter = new ListAdapper<Category, CategoryView>(categories, categoryProvider){
            @Override
            public List<Category> getNextVisibleList() {
                Collections.sort(GroupableAdapper.this.source, comparator);
                final List<? extends Model> nextVisibleList = internalListAdapter.getNextVisibleList();
                return new FunctionList<>(generateCategoryPositions(nextVisibleList, comparator), new FunctionList.Function<Integer, Category>() {
                    @Override
                    public Category evaluate(Integer input) {
                        return comparator.getGroup(nextVisibleList.get(input));
                    }
                });
            }
        };

        layouts = SetUtils.newSet();
        layouts.addAll(internalListAdapter.getViewTypes());
        layouts.addAll(internalCategoryAdapter.getViewTypes());
    }

    @Override
    protected void onChanged() {
        internalListAdapter.notifyDataSetChanged();
        categoryPositions = generateCategoryPositions(internalListAdapter.getVisibleList(), comparator);
        internalCategoryAdapter.notifyDataSetChanged();
    }

    /**
     * Returns a list, each entry of which represents the first index in the provided {@param list} for each group (group being defined by the provided {@param comparator})
     */
    private static <Model, Category> List<Integer> generateCategoryPositions(List<? extends Model> list, GroupComparator<Model, Category> comparator) {
        Collections.sort(list, comparator);
        List<Integer> positions = new ArrayList<>();
        if (!list.isEmpty()) {
            positions.add(0);
            for (int i = 1; i < list.size(); i++) {
                Category lhs = comparator.getGroup(list.get(i - 1));
                Category rhs = comparator.getGroup(list.get(i));
                if (comparator.getGroupComparator().compare(lhs, rhs) != 0) {
                    positions.add(i);
                }
            }
        }
        return positions;
    }

    @NonNull
    @Override
    public Set<Integer> getViewTypes() {
        return layouts;
    }

    @Override
    public int getItemCount() {
        return internalListAdapter.getItemCount() + categoryPositions.size();
    }

    @SuppressWarnings("UnusedDeclaration")
    public GroupableAdapper<Model, ModelView, Category, CategoryView> setFilterFunction(FilterFunction<? super Model> filterFunction) {
        internalListAdapter.setFilterFunction(filterFunction);
        return this;
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new CompositeFilter(internalListAdapter.getFilter()){
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    super.publishResults(constraint, results);
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }

    Filter filter;

    @Override
    public SparseBooleanArray getInsertions() {
        SparseBooleanArray insertions = cleanInsertions(internalListAdapter.getInsertions());
        SparseBooleanArray categoryInsertions = cleanCategoryInsertions(internalCategoryAdapter.getInsertions(), generateCategoryPositions(internalListAdapter.getNextVisibleList(), comparator));
        return SparseArrayUtils.combine(insertions, categoryInsertions);
    }

    @Override
    public SparseBooleanArray getDeletions() {
        SparseBooleanArray deletions = cleanDeletions(internalListAdapter.getDeletions(), categoryPositions);
        SparseBooleanArray categoryDeletions = cleanCategoryDeletions(internalCategoryAdapter.getDeletions(), categoryPositions);
        return SparseArrayUtils.combine(deletions, categoryDeletions);
    }

    @Override
    public SparseIntArray getReorderings() {
        List<Integer> nextCategoryPositions = generateCategoryPositions(internalListAdapter.getNextVisibleList(), comparator);
        SparseIntArray reorderings = cleanReorderings(internalListAdapter.getReorderings(), categoryPositions, nextCategoryPositions);
        SparseIntArray categoryReorderings = cleanCategoryReorderings(internalCategoryAdapter.getReorderings(), categoryPositions, nextCategoryPositions);
        return SparseArrayUtils.combine(reorderings, categoryReorderings);
    }

    @Override
    public int getItemViewType(int position) {
        Pair<Integer, BaseAdapper> pair = getDelegate(position, categoryPositions);
        return pair.second.getItemViewType(pair.first);
    }

    @Override
    public boolean isModel(int position) {
        Pair<Integer, BaseAdapper> pair = getDelegate(position, categoryPositions);
        return pair.second == internalListAdapter;
    }

    @Override
    public Object getItem(int position) {
        Pair<Integer, BaseAdapper> pair = getDelegate(position, categoryPositions);
        return pair.second.getItem(pair.first);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Pair<Integer, BaseAdapper> pair = getDelegate(position, categoryPositions);
        pair.second.onBindViewHolder(holder, pair.first);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseAdapper adapper = getAdapperForViewType(viewType);
        return adapper.onCreateViewHolder(parent, viewType);
    }

    private BaseAdapper getAdapperForViewType(int viewType) {
        return internalListAdapter.getViewTypes().contains(viewType) ? internalListAdapter : internalCategoryAdapter;
    }

    @Override
    public Model getModel(int position) {
        return internalListAdapter.getModel(superPositionToIndex(position, categoryPositions));
    }

    public Pair<Integer, BaseAdapper> getDelegate(int position, List<Integer> categoryPositions) {
        int index = superPositionToIndex(position, categoryPositions);
        if(index < 0) {
            return new Pair<Integer, BaseAdapper>(-index - 1, internalCategoryAdapter);
        } else {
            return new Pair<Integer, BaseAdapper>(index, internalListAdapter);
        }
    }

    /**
     * @return the index in either the item list, or the category positions list
     * If the return value is zero or positive, it is the index of the item list (and consequently the position points to an item)
     * If the return value is negative, it is the negative of the index of the category positions list, were the list 1 indexed (and consequently the position points to a category header)
     * -1 => categoryPositions[0]
     * -2 => categoryPositions[1]
     * etc
     */
    private static int superPositionToIndex(int position, List<Integer> categoryPositions) {
        if(categoryPositions.isEmpty()) {
            return position;
        }
        for (int i = 0; i < categoryPositions.size() - 1; i++) {
            if (categoryPositions.get(i) + i == position) {
                return -(i + 1);
            }
            for (int j = categoryPositions.get(i); j < categoryPositions.get(i + 1); j++) {
                if (j + i + 1 == position) {
                    return j;
                }
            }
        }
        if (position == categoryPositions.get(categoryPositions.size() - 1) + categoryPositions.size() - 1) {
            return -categoryPositions.size();
        } else {
            return position - categoryPositions.size();
        }
    }

    public int listIndexToSuperPosition(int index) {
        return listIndextoSuperPosition(index, categoryPositions);
    }

    private static int listIndextoSuperPosition(int index, List<Integer> categoryPositions) {
        int position = index;
        for(Integer integer : categoryPositions) {
            if(integer <= index) {
                position++;
            } else {
                break;
            }
        }
        return position;

    }

    private static int categoryIndextoSuperPosition(int index, List<Integer> categoryPositions) {
        return categoryPositions.isEmpty() ? index : index + categoryPositions.get(index);
    }

    private SparseBooleanArray cleanInsertions(SparseBooleanArray array) {
        SparseBooleanArray cleanArray = new SparseBooleanArray(array.size());
        for(int i = 0; i < array.size(); i++) {
            int key = array.keyAt(i);
            boolean value = array.valueAt(i);
            cleanArray.put(listIndextoSuperPosition(key, generateCategoryPositions(internalListAdapter.getNextVisibleList(), comparator)), value);
        }

        return cleanArray;
    }

    private static SparseBooleanArray cleanDeletions(SparseBooleanArray array, List<Integer> categoryPositions) {
        SparseBooleanArray cleanArray = new SparseBooleanArray(array.size());
        for(int i = 0; i < array.size(); i++) {
            int key = array.keyAt(i);
            boolean value = array.valueAt(i);
            cleanArray.put(listIndextoSuperPosition(key, categoryPositions), value);
        }

        return cleanArray;
    }

    private static SparseIntArray cleanReorderings(SparseIntArray reorderings, List<Integer> categoryPositions, List<Integer> nextCategoryPositions) {
        SparseIntArray cleanArray = new SparseIntArray(reorderings.size());
        for(int i = 0; i < reorderings.size(); i++) {
            int key = reorderings.keyAt(i);
            int value = reorderings.valueAt(i);
            cleanArray.put(
                    listIndextoSuperPosition(key, nextCategoryPositions),
                    listIndextoSuperPosition(value, categoryPositions)
            );
        }
        return cleanArray;
    }

    private static SparseBooleanArray cleanCategoryInsertions(SparseBooleanArray array, List<Integer> nextCategoryPositions) {
        SparseBooleanArray cleanArray = new SparseBooleanArray(array.size());
        for(int i = 0; i < array.size(); i++) {
            int key = array.keyAt(i);
            boolean value = array.valueAt(i);
            cleanArray.put(categoryIndextoSuperPosition(key, nextCategoryPositions), value);
        }

        return cleanArray;
    }
    private static SparseBooleanArray cleanCategoryDeletions(SparseBooleanArray array, List<Integer> categoryPositions) {
        SparseBooleanArray cleanArray = new SparseBooleanArray(array.size());
        for(int i = 0; i < array.size(); i++) {
            int key = array.keyAt(i);
            boolean value = array.valueAt(i);
            cleanArray.put(categoryIndextoSuperPosition(key, categoryPositions), value);
        }

        return cleanArray;
    }
    private static SparseIntArray cleanCategoryReorderings(SparseIntArray reorderings, List<Integer> categoryPositions, List<Integer> nextCategoryPositions) {
        SparseIntArray cleanArray = new SparseIntArray(reorderings.size());
        for(int i = 0; i < reorderings.size(); i++) {
            int key = reorderings.keyAt(i);
            int value = reorderings.valueAt(i);
            cleanArray.put(
                    categoryIndextoSuperPosition(key, nextCategoryPositions),
                    categoryIndextoSuperPosition(value, categoryPositions)
            );
        }
        return cleanArray;
    }

    @Override
    public boolean isGroup(int position) {
        return superPositionToIndex(position, categoryPositions) < 0;
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
    public GroupableAdapper<Model, ModelView, Category, CategoryView> setSelectionManager(@Nullable SelectionManager<? extends Model> selectionManager) {
        internalListAdapter.setSelectionManager(new GroupableAdapperSelectManager<>(this, selectionManager));
        super.setSelectionManager(selectionManager);
        return this;
    }

    private static class GroupableAdapperSelectManager<T> extends ConversionSelectManager<T> {
        private final GroupableAdapper adapper;

        public GroupableAdapperSelectManager(GroupableAdapper adapper, SelectionManager<T> delegate) {
            super(delegate);
            this.adapper = adapper;
        }

        @Override
        protected int convertPosition(int position) {
            return adapper.listIndexToSuperPosition(position);
        }
    }

    private static class GroupableIdentifier<T> implements Bidentifier<T> {
        private final GroupableAdapper<T, ? extends View, ?, ? extends View> adapper;

        private GroupableIdentifier(GroupableAdapper<T, ? extends View, ?, ? extends View> adapper) {
            this.adapper = adapper;
        }

        @Override
        public long getId(int position) {
            Pair<Integer, BaseAdapper> pair = adapper.getDelegate(position, adapper.categoryPositions);
            return pair.second.getItemId(pair.first);
        }

        @Override
        public Set<? extends T> getModels(Set<Long> ids) {
            return adapper.internalListAdapter.getItems(ids);
        }
    }
}
