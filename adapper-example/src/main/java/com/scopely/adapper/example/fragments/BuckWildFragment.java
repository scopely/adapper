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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.scopely.adapper.adapters.BaseAdapper;
import com.scopely.adapper.adapters.CursorAdapper;
import com.scopely.adapper.adapters.GroupableAdapper;
import com.scopely.adapper.adapters.RecursiveAdapper;
import com.scopely.adapper.adapters.SingleViewAdapper;
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
import com.scopely.adapper.interfaces.MiniOrm;
import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.selection.MultiSelectManager;

import java.util.List;
import java.util.Set;

/**
 * Part of the Scopely™ Platform
 * © 2016 Scopely, Inc.
 */
public class BuckWildFragment extends AbstractFragment<RecursiveAdapper<NameAndMood>> {

    public static final String TABLE_NAME = "name";
    public static final String COLUMN_NAME_ID = "_id";
    public static final LinearLayout.LayoutParams PARAMS = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private SQLiteDatabase db;
    private Runnable toggler;
    private int count = 12;
    private final List<NameAndMood> list = Utils.generateNameAndMoodList(count);
    private MultiSelectManager<NameAndMood> selectionManager;

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH, MENU_ITEMS_SELECT, MENU_ITEMS_ANIMATION};
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteAssetHelper(getActivity(), "example.db", null, 1) {}
                .getReadableDatabase();
    }

    @Override
    public void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    protected void reorder() {
        toggler.run();
    }

    @Override
    protected void add() {
        list.add(new NameAndMood(NameAndMood.Mood.random(), "Just added #" + count));
        count++;
    }

    @Override
    protected void remove() {
        if(list.isEmpty()){
            Toast.makeText(getActivity(), "List is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        list.remove(0);
    }

    @Override
    protected RecursiveAdapper<NameAndMood> createAdapter() {
        ViewProvider<NameAndMood, NameAndMoodView> provider = new PopulatableProvider<NameAndMood, NameAndMoodView>(R.layout.view_name_and_mood) {
            @Override
            public int getViewType(NameAndMood nameAndMood) {
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
                if(recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    view.setGridMode(true);
                    PARAMS.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    view.imageView.setLayoutParams(PARAMS);
                    view.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    view.imageView.setAdjustViewBounds(true);
                } else {
                    view.setGridMode(false);
                    PARAMS.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    view.imageView.setLayoutParams(PARAMS);
                    view.imageView.setAdjustViewBounds(false);
                }
            }
        };

        GroupComparator<NameAndMood, NameAndMood.Mood> comparator = new GroupComparatorImpl<NameAndMood, NameAndMood.Mood>() {
            @Override
            protected int groupCompare(NameAndMood.Mood lhs, NameAndMood.Mood rhs) {
                return lhs.compareTo(rhs);
            }

            @Override
            protected int itemCompare(NameAndMood lhs, NameAndMood rhs) {
                return 0;
            }

            @Override
            public NameAndMood.Mood getGroup(NameAndMood item) {
                return item.mood;
            }
        };

        ViewProvider<NameAndMood.Mood, TextView> categoryProvider = new ViewProviderImpl<NameAndMood.Mood, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            public int getViewType(NameAndMood.Mood mood) {
                return android.R.layout.simple_list_item_1;
            }

            @Override
            protected void bind(TextView view, NameAndMood.Mood mood, int position, @Nullable SelectionManager selectionManager) {
                view.setGravity(Gravity.CENTER_HORIZONTAL);
                view.setText(mood.toString());
            }
        };

        MiniOrm<NameAndMood> miniOrm = new MiniOrm<NameAndMood>() {
            @Override
            public NameAndMood getObject(Cursor c) {
                NameAndMood.Mood mood = NameAndMood.Mood.valueOf(c.getString(c.getColumnIndex("mood")));
                String name = c.getString(c.getColumnIndex("name"));
                return new NameAndMood(mood, name);
            }
        };

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_single_buck_wild, null);

        final GroupableAdapper<NameAndMood, NameAndMoodView, NameAndMood.Mood, TextView> groupable = new GroupableAdapper<>(list, provider, comparator, categoryProvider);
        groupable.setFilterFunction(new FilterFunction<NameAndMood>() {
            @Override
            public boolean filter(NameAndMood item, @Nullable CharSequence constraint) {
                return constraint != null && item.name.contains(constraint);
            }
        });
        final SingleViewAdapper singleView = new SingleViewAdapper(view);
        singleView.setFilterFunction(new Predicate<CharSequence>() {
            @Override
            public boolean apply(CharSequence charSequence) {
                return charSequence == null || charSequence.length() == 0;
            }
        });
        final Cursor cursor = query(db, null);
        CursorAdapper.CursorIdentifier identifier = new CursorAdapper.ColumnIdentifier(COLUMN_NAME_ID);
        CursorAdapper.CursorLookup<NameAndMood> lookup = new CursorAdapper.ColumnLookup<>(db, TABLE_NAME, COLUMN_NAME_ID);
        CursorAdapper<NameAndMood, NameAndMoodView> cursorAdapper = new CursorAdapper<>(cursor, identifier, lookup, miniOrm, provider);
        cursorAdapper.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return query(db, constraint);
            }
        });
        final RecursiveAdapper<NameAndMood> recursiveAdapper = new RecursiveAdapper<>(groupable, singleView, cursorAdapper);
        InvertedSpanSizeLookup spanSizeLookup = new InvertedSpanSizeLookup(1, 5, 8) {
            @Override
            public int getItemsPerRow(int position) {
                Pair<BaseAdapper<? extends NameAndMood, ? extends RecyclerView.ViewHolder>, Integer> pair = recursiveAdapper.getInternalAdapter(position);
                if(pair.first == groupable) {
                    return groupable.isGroup(pair.second) ? 1 : 5;
                } else if (pair.first == singleView) {
                    return 1;
                } else {
                    return 8;
                }
            }
        };
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanSizeLookup.getRequiredSpans());
        gridLayoutManager.setSpanSizeLookup(spanSizeLookup);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        toggler = new Runnable() {
            boolean grid;
            @Override
            public void run() {
                int pos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                grid = !grid;
                recyclerView.setLayoutManager(grid ? gridLayoutManager : linearLayoutManager);
                recyclerView.scrollToPosition(pos);
            }
        };
        selectionManager = new MultiSelectManager<NameAndMood>(recursiveAdapper, 10) {
            @Override
            public boolean selectItem(int position, boolean selected) {
                boolean bool = super.selectItem(position, selected);
                getActivity().invalidateOptionsMenu();
                return bool;
            }

            @Override
            public void clearSelections() {
                super.clearSelections();
                getActivity().invalidateOptionsMenu();
            }

            @Override
            protected void onMaximumExceeded(int maximumSelectable) {
                Toast.makeText(getActivity(), "You may only select 10 concurrent items", Toast.LENGTH_LONG).show();
            }
        };
        recursiveAdapper.setSelectionManager(selectionManager);
        return recursiveAdapper;
    }

    private Cursor query(SQLiteDatabase db, @Nullable CharSequence constraint) {
        String selection = constraint != null && constraint.length() > 0 ? "name LIKE '%" + constraint.toString() + "%'" : null;
        return db.query(TABLE_NAME, null, selection, null, null, null, null);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean show = selectionManager != null && selectionManager.getCount() > 0;
        for(int integer : MENU_ITEMS_SELECT) {
            menu.findItem(integer).setVisible(show);
        }
    }
}
