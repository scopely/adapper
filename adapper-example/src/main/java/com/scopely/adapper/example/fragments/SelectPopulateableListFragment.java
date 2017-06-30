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

import android.support.annotation.Nullable;
import android.view.View;

import com.scopely.adapper.adapters.ListAdapper;
import com.scopely.adapper.example.R;
import com.scopely.adapper.example.Utils;
import com.scopely.adapper.example.objects.NameAndMood;
import com.scopely.adapper.example.views.NameAndMoodView;
import com.scopely.adapper.impls.PopulatableProvider;
import com.scopely.adapper.interfaces.FilterFunction;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.selection.RadioSelectManager;

import java.util.List;
import java.util.Set;

public class SelectPopulateableListFragment extends AbstractFragment<ListAdapper> {
    private int count = 100;
    private List<NameAndMood> list = Utils.generateNameAndMoodList(count);

    protected ListAdapper<NameAndMood, NameAndMoodView> createAdapter() {
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
                } else {
                    view.setChecked(false);
                    view.setOnClickListener(null);
                }
            }
        };

        ListAdapper<NameAndMood, NameAndMoodView> adapper = new ListAdapper<>(list, provider).setFilterFunction(new FilterFunction<NameAndMood>() {
            @Override
            public boolean filter(NameAndMood item, @Nullable CharSequence constraint) {
                return constraint == null || item.name.contains(constraint);
            }
        });
        adapper.setSelectionManager(new RadioSelectManager<>(adapper));
        return adapper;
    }

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH, MENU_ITEMS_SELECT};
    }
}