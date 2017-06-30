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
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;
import android.widget.Toast;

import com.scopely.adapper.impls.TypedViewHolder;
import com.scopely.adapper.adapters.BaseAdapper;
import com.scopely.adapper.adapters.ListAdapper;
import com.scopely.adapper.example.Utils;
import com.scopely.adapper.impls.ViewProviderImpl;
import com.scopely.adapper.interfaces.FilterFunction;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.interfaces.SelectionManager;

import java.util.List;
import java.util.Set;

public class SimpleListFragment extends AbstractFragment {

    private int count = 100;
    private List<String> list = Utils.generateStringList(count);

    protected BaseAdapper<String, TypedViewHolder<? super String,? extends TextView>> createAdapter() {
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

        ListAdapper<String, TextView> adapter = new ListAdapper<>(list, provider);
        adapter.setFilterFunction(new FilterFunction<String>() {
            @Override
            public boolean filter(String item, @Nullable CharSequence constraint) {
                return constraint == null || item.contains(constraint);
            }
        });
        return adapter;
    }

    @Override
    protected void reorder() {
        if(list.isEmpty()){
            Toast.makeText(getActivity(), "List is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        list.add(getLastVisibleItemPosition(), list.remove(getFirstVisibleItemPosition()));
    }

    private int getFirstVisibleItemPosition() {
        return ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    private int getLastVisibleItemPosition() {
        return ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
    }

    @Override
    protected void add() {
        list.add(getFirstVisibleItemPosition(), "Just added #" + count);
        count++;
    }

    @Override
    protected void remove() {
        if(list.isEmpty()){
            Toast.makeText(getActivity(), "List is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        list.remove(getFirstVisibleItemPosition());
    }

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH, MENU_ITEMS_ANIMATION};
    }

}
