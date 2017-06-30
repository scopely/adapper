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
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.scopely.adapper.adapters.GroupableAdapper;
import com.scopely.adapper.example.R;
import com.scopely.adapper.example.Utils;
import com.scopely.adapper.example.objects.NameAndMood;
import com.scopely.adapper.example.views.NameAndMoodView;
import com.scopely.adapper.impls.GroupComparatorImpl;
import com.scopely.adapper.impls.PopulatableProvider;
import com.scopely.adapper.impls.ViewProviderImpl;
import com.scopely.adapper.interfaces.FilterFunction;
import com.scopely.adapper.interfaces.GroupComparator;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.selection.MultiSelectManager;

import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SelectGroupableListFragment extends AbstractFragment<GroupableAdapper> {

    private int count = 9;
    private List<NameAndMood> list = Utils.generateNameAndMoodList(count);

    @Override
    protected GroupableAdapper<NameAndMood, NameAndMoodView, NameAndMood.Mood, TextView> createAdapter() {
        ViewProvider<NameAndMood, NameAndMoodView> provider = new PopulatableProvider<NameAndMood, NameAndMoodView>(R.layout.view_name_and_mood) {
            @Override
            public int getViewType(NameAndMood s) {
                return R.layout.view_name_and_mood;
            }

            @Override
            protected void bind(NameAndMoodView view, NameAndMood nameAndMood, final int position, @Nullable final SelectionManager selectionManager) {
                super.bind(view, nameAndMood, position, selectionManager);
                if (selectionManager != null) {
                    view.setChecked(selectionManager.isItemSelected(position));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectionManager.selectItem(position, !selectionManager.isItemSelected(position));
                        }
                    });
                }
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
                view.setText(mood.toString());
            }
        };


        GroupableAdapper<NameAndMood, NameAndMoodView, NameAndMood.Mood, TextView> groupableAdapter = new GroupableAdapper<>(list, provider, comparator, groupProvider);
        groupableAdapter.setFilterFunction(new FilterFunction<NameAndMood>() {
            @Override
            public boolean filter(NameAndMood item, @Nullable CharSequence constraint) {
                return constraint != null && item.name.contains(constraint);
            }
        });
        groupableAdapter.setSelectionManager(new MultiSelectManager<>(groupableAdapter));
        return groupableAdapter;
    }

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH, MENU_ITEMS_SELECT};
    }
}
