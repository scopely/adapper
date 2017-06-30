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

import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.scopely.adapper.selection.MultiSelectManager;

import java.util.List;
import java.util.Set;

public class SelectRecursiveFragment extends AbstractFragment {

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
            protected void bind(TextView view, String s, final int position, final SelectionManager selectionManager) {
                view.setText(s);
                if (selectionManager != null) {
                    view.setTransformationMethod(new TransformationMethod() {
                        @Override
                        public CharSequence getTransformation(CharSequence source, View view) {
                            return selectionManager.isItemSelected(position) ? source + " (selected)" : source;
                        }

                        @Override
                        public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {

                        }
                    });
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectionManager.selectItem(position, !selectionManager.isItemSelected(position));
                        }
                    });
                }
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

        ListAdapper<NameAndMood, NameAndMoodView> adapter2 = new ListAdapper<>(list2, provider2);
        adapter2.setFilterFunction(new FilterFunction<NameAndMood>() {
            @Override
            public boolean filter(NameAndMood item, @Nullable CharSequence constraint) {
                return constraint == null || item.name.contains(constraint);
            }
        });

        SingleViewAdapper adapter3 = new SingleViewAdapper(LayoutInflater.from(getActivity()).inflate(R.layout.view_single, null));

        RecursiveAdapper<Object> adapper = new RecursiveAdapper<Object>(adapter1, adapter3, adapter2);
        adapper.setSelectionManager(new MultiSelectManager<Object>(adapper, 3){
            @Override
            protected void onMaximumExceeded(int maximumSelectable) {
                Toast.makeText(getActivity(), "You may only select 3 concurrent items", Toast.LENGTH_LONG).show();
            }
        });
        return adapper;
    }

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH, MENU_ITEMS_SELECT};
    }
}
