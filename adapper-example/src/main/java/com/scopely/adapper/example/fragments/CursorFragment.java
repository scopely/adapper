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
import android.widget.FilterQueryProvider;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.scopely.adapper.adapters.CursorAdapper;
import com.scopely.adapper.example.R;
import com.scopely.adapper.example.objects.NameAndMood;
import com.scopely.adapper.example.views.NameAndMoodView;
import com.scopely.adapper.impls.PopulatableProvider;
import com.scopely.adapper.interfaces.MiniOrm;
import com.scopely.adapper.interfaces.ViewProvider;

import java.util.Set;

public class CursorFragment extends AbstractFragment<CursorAdapper> {

    public static final String TABLE_NAME = "name";
    public static final String COLUMN_NAME_ID = "_id";
    private SQLiteDatabase db;

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
    protected CursorAdapper createAdapter() {
        ViewProvider<NameAndMood, NameAndMoodView> provider = new PopulatableProvider<NameAndMood, NameAndMoodView>(R.layout.view_name_and_mood) {
            @Override
            public int getViewType(NameAndMood s) {
                return R.layout.view_name_and_mood;
            }
        };

        final Cursor cursor = query(db, null);

        MiniOrm<NameAndMood> miniOrm = new MiniOrm<NameAndMood>() {
            @Override
            public NameAndMood getObject(Cursor c) {
                NameAndMood.Mood mood = NameAndMood.Mood.valueOf(c.getString(c.getColumnIndex("mood")));
                String name = c.getString(c.getColumnIndex("name"));
                return new NameAndMood(mood, name);
            }
        };
        CursorAdapper.CursorIdentifier identifier = new CursorAdapper.ColumnIdentifier(COLUMN_NAME_ID);
        CursorAdapper.CursorLookup<NameAndMood> lookup = new CursorAdapper.ColumnLookup<>(db, TABLE_NAME, COLUMN_NAME_ID);
        final CursorAdapper adapter = new CursorAdapper<>(cursor, identifier, lookup, miniOrm, provider);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return query(db, constraint);
            }
        });
        return adapter;
    }

    private Cursor query(SQLiteDatabase db, @Nullable CharSequence constraint) {
        String selection = constraint != null && constraint.length() > 0 ? "name LIKE '%" + constraint.toString() + "%'" : null;
        return db.query(TABLE_NAME, null, selection, null, null, null, null);
    }

    @Override
    protected Set<Integer>[] getActionSets() {
        return new Set[]{MENU_ITEMS_SEARCH};
    }

}
