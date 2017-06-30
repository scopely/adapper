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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.Toast;

import com.scopely.adapper.adapters.BaseAdapper;
import com.scopely.adapper.example.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public abstract class AbstractFragment<T extends BaseAdapper> extends Fragment {
    protected final static Set<Integer> MENU_ITEMS_ANIMATION = new HashSet<>(Arrays.asList(R.id.menu_add, R.id.menu_subtract, R.id.menu_reorder));
    protected final static Set<Integer> MENU_ITEMS_SELECT = new HashSet<>(Arrays.asList(R.id.action_clear, R.id.action_describe));
    protected final static Set<Integer> MENU_ITEMS_SEARCH = new HashSet<>(Arrays.asList(R.id.action_search));
    private final Set<Integer> MENU_ITEMS = new HashSet<>(combine(getActionSets()));

    protected abstract Set<Integer>[] getActionSets();

    protected RecyclerView recyclerView;
    protected T adapter;

    public AbstractFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_simple_list, container, false);
        recyclerView = (RecyclerView) root.findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = createAdapter();
        recyclerView.setAdapter(adapter);
        return root;
    }

    protected abstract T createAdapter();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_filterable, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchItem = (SearchView) item.getActionView();
        searchItem.setQueryHint(getActivity().getString(R.string.filter));
        searchItem.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(adapter != null && adapter instanceof Filterable){
                    ((Filterable) adapter).getFilter().filter(s);
                }
                return true;
            }
        });

        inflater.inflate(R.menu.fragment_selector, menu);
        inflater.inflate(R.menu.fragment_animated_adapter, menu);

        for(Set<Integer> set : new Set[]{MENU_ITEMS_ANIMATION, MENU_ITEMS_SEARCH, MENU_ITEMS_SELECT}) {
            for (int integer : set) {
                if (!MENU_ITEMS.contains(integer)) {
                    menu.findItem(integer).setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_clear){
            clear();
            return true;
        }
        if(item.getItemId() == R.id.action_describe){
            describe();
            return true;
        }
        if(item.getItemId() == R.id.menu_add){
            add();
            adapter.update();
            return true;
        }
        if(item.getItemId() == R.id.menu_subtract){
            remove();
            adapter.update();
            return true;
        }
        if(item.getItemId() == R.id.menu_reorder){
            reorder();
            adapter.update();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void describe(){
        Set set = adapter.getSelections();
        StringBuilder builder = new StringBuilder();
        if(!set.isEmpty()) {
            builder.append("Currently selected: \n");
            boolean first = true;
            for (Object object : set) {
                if (!first) {
                    builder.append(",\n");
                }
                first = false;
                builder.append(object.toString());
            }
        } else {
            builder.append("None selected");
        }
        Toast.makeText(getActivity(), builder, Toast.LENGTH_LONG).show();
    }

    private void clear() {
        adapter.clearSelections();
        adapter.notifyDataSetChanged();
    }

    private Set<Integer> combine(Set<Integer>... sets) {
        HashSet<Integer> combined = new HashSet<>();
        for(Set<Integer> set : sets) {
            combined.addAll(set);
        }
        return combined;
    }

    protected void reorder(){};

    protected void add(){};

    protected void remove(){};

}
