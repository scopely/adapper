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
import android.view.LayoutInflater;
import android.widget.TextView;

import com.scopely.adapper.adapters.BaseAdapper;
import com.scopely.adapper.adapters.ListAdapper;
import com.scopely.adapper.adapters.RecursiveAdapper;
import com.scopely.adapper.adapters.SingleViewAdapper;
import com.scopely.adapper.example.R;
import com.scopely.adapper.example.Utils;
import com.scopely.adapper.example.objects.NameAndMood;
import com.scopely.adapper.example.views.NameAndMoodView;
import com.scopely.adapper.impls.PopulatableProvider;
import com.scopely.adapper.impls.ViewProviderImpl;
import com.scopely.adapper.interfaces.FilterFunction;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.interfaces.SelectionManager;

import java.util.List;
import java.util.Set;

public class RecursiveFragment extends AbstractFragment {

    private List<String> list = Utils.generateStringList(5);
    private List<NameAndMood> list2 = Utils.generateNameAndMoodList(5);

    @Override
    protected BaseAdapper createAdapter() {
        ViewProvider<String, TextView> provider = new ViewProviderImpl<String, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            public int getViewType(String s) {
                return android.R.layout.simple_list_item_1;
            }

            @Override
            protected void bind(TextView view, String s, int position, SelectionManager selectionManager) {
                view.setText(s);
            }
        };

        ListAdapper<String, TextView> adapter1 = new ListAdapper<>(list, provider);
        adapter1.setFilterFunction(new FilterFunction<String>() {
            @Override
            public boolean filter(String item, @Nullable CharSequence constraint) {
                return constraint == null || item.contains(constraint);
            }
        });

        ViewProvider<NameAndMood, NameAndMoodView> provider2 = new PopulatableProvider<NameAndMood, NameAndMoodView>(R.layout.view_name_and_mood) {
            @Override
            public int getViewType(NameAndMood s) {
                return R.layout.view_name_and_mood;
            }

        };

        ListAdapper<NameAndMood, NameAndMoodView> adapter2 = new ListAdapper<>(list2, provider2);
        adapter2.setFilterFunction(new FilterFunction<NameAndMood>() {
            @Override
            public boolean filter(NameAndMood item, @Nullable CharSequence constraint) {
                return constraint == null || item.name.contains(constraint);
            }
        });

        SingleViewAdapper adapter3 = new SingleViewAdapper(LayoutInflater.from(getActivity()).inflate(R.layout.view_single, null));

        return new RecursiveAdapper<>(adapter1, adapter3, adapter2);
    }

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH};
    }
}
