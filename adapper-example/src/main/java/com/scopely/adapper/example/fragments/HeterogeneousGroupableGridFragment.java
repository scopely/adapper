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

package com.scopely.adapper.example.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scopely.adapper.impls.TypedViewHolder;
import com.scopely.adapper.adapters.GroupableAdapper;
import com.scopely.adapper.example.R;
import com.scopely.adapper.example.Utils;
import com.scopely.adapper.example.objects.NameAndMood;
import com.scopely.adapper.example.views.NameAndMoodView;
import com.scopely.adapper.extras.InvertedSpanSizeLookup;
import com.scopely.adapper.impls.GroupComparatorImpl;
import com.scopely.adapper.impls.PopulatableProvider;
import com.scopely.adapper.impls.ViewProviderImpl;
import com.scopely.adapper.interfaces.FilterFunction;
import com.scopely.adapper.interfaces.GroupComparator;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.interfaces.SelectionManager;

import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class HeterogeneousGroupableGridFragment extends AbstractFragment<GroupableAdapper<NameAndMood, NameAndMoodView, NameAndMood.Mood, TextView>> {
    private List<NameAndMood> list = Utils.generateNameAndMoodList(12);
    private GridLayoutManager manager;

    @Override
    protected GroupableAdapper<NameAndMood, NameAndMoodView, NameAndMood.Mood, TextView> createAdapter() {
        ViewProvider<NameAndMood, NameAndMoodView> provider = new PopulatableProvider<NameAndMood, NameAndMoodView>(R.layout.view_name_and_mood) {
            @Override
            public int getViewType(NameAndMood s) {
                return R.layout.view_name_and_mood;
            }

            @Override
            public TypedViewHolder<NameAndMood, NameAndMoodView> create(LayoutInflater inflater, ViewGroup parent, int viewType) {
                TypedViewHolder<NameAndMood, NameAndMoodView> holder = super.create(inflater, parent, viewType);
                holder.getView().textView.setVisibility(View.GONE);
                ImageView imageView = holder.getView().imageView;
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);
                return holder;
            }
        };

        GroupComparator<NameAndMood, NameAndMood.Mood> comparator = new GroupComparatorImpl<NameAndMood, NameAndMood.Mood>() {
            @Override
            public NameAndMood.Mood getGroup(NameAndMood nameAndMood) {
                return nameAndMood.mood;
            }

            @Override
            protected int groupCompare(NameAndMood.Mood lhs, NameAndMood.Mood rhs) {
                return lhs.compareTo(rhs);
            }

            @Override
            protected int itemCompare(NameAndMood lhs, NameAndMood rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        };

        ViewProvider<NameAndMood.Mood, TextView> groupProvider = new ViewProviderImpl<NameAndMood.Mood, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            public int getViewType(NameAndMood.Mood mood) {
                return android.R.layout.simple_list_item_1;
            }

            @Override
            protected void bind(TextView view, NameAndMood.Mood mood, int position, SelectionManager selectionManager) {
                view.setGravity(Gravity.CENTER_HORIZONTAL);
                view.setText(mood.toString());
            }
        };

        FilterFunction<NameAndMood> filterFunction = new FilterFunction<NameAndMood>() {
            @Override
            public boolean filter(NameAndMood item, @Nullable CharSequence constraint) {
                return constraint != null && item.name.contains(constraint);
            }
        };

        GroupableAdapper<NameAndMood, NameAndMoodView, NameAndMood.Mood, TextView> groupableAdapter = new GroupableAdapper<>(list, provider, comparator, groupProvider);
        groupableAdapter.setFilterFunction(filterFunction);
        return groupableAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        InvertedSpanSizeLookup spanSizeLookup = new InvertedSpanSizeLookup(1, 5, 10) {
            @Override
            public int getItemsPerRow(int position) {
                if (adapter.isGroup(position)) {
                    return 1;
                } else {
                    NameAndMood nameAndMood = adapter.getModel(position);
                    if (nameAndMood.mood == NameAndMood.Mood.HAPPY) {
                        return 5;
                    } else {
                        return 10;
                    }
                }
            }
        };
        manager = new GridLayoutManager(inflater.getContext(), spanSizeLookup.getRequiredSpans());
        manager.setSpanSizeLookup(spanSizeLookup);
        recyclerView.setLayoutManager(manager);
        return view;
    }

    @Override
    protected void reorder() {
        Toast.makeText(getActivity(), "Groupables are naturally sorted, reordering is not possible", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void add() {
        list.add(getFirstVisibleItemPosition(), new NameAndMood(NameAndMood.Mood.random(), "Just added"));
    }

    @Override
    protected void remove() {
        if(list.isEmpty()){
            Toast.makeText(getActivity(), "List is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        list.remove(getFirstVisibleItemPosition());
    }

    private int getFirstVisibleItemPosition() {
        return manager.findFirstCompletelyVisibleItemPosition();
    }

    private int getLastVisibleItemPosition() {
        return manager.findLastCompletelyVisibleItemPosition();
    }

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH, MENU_ITEMS_ANIMATION};
    }
}
