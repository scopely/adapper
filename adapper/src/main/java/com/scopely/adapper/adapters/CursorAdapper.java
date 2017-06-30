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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;

import com.scopely.adapper.impls.TypedViewHolder;
import com.scopely.adapper.interfaces.Bidentifier;
import com.scopely.adapper.interfaces.MiniOrm;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.utils.SetUtils;

import java.util.Collections;
import java.util.Set;

import static android.support.v7.widget.RecyclerView.NO_ID;

/**
 * An Adapper that displays a {@link Cursor}
 * @param <Model> The class of object which a single row maps to
 * @param <GenericView> The {@link View} class used to display the items in the list
 */
public class CursorAdapper<Model, GenericView extends View> extends BaseAdapper<Model, TypedViewHolder<? super Model, ? extends GenericView>> implements Filterable {
    @Nullable
    private Cursor cursor;
    private final MiniOrm<Model> miniOrm;
    protected final ViewProvider<? super Model, ? extends GenericView> provider;
    private CursorFilter filter;
    private FilterQueryProvider filterQueryProvider;

    public CursorAdapper(Cursor cursor, MiniOrm<Model> miniOrm, ViewProvider<? super Model, ? extends GenericView> provider) {
        this(cursor, null, null, miniOrm, provider);
    }

    public CursorAdapper(final Cursor cursor, @Nullable final CursorIdentifier identifier, @Nullable final CursorLookup<Model> lookup, MiniOrm<Model> miniOrm, ViewProvider<? super Model, ? extends GenericView> provider) {
        this.cursor = cursor;
        this.miniOrm = miniOrm;
        this.provider = provider;
        if(identifier != null && lookup != null) {
            setBidentifier(new Bidentifier<Model>() {
                @Override
                public Set<Model> getModels(Set<Long> ids) {
                    return lookup.getModels(ids, CursorAdapper.this.miniOrm);
                }

                @Override
                public long getId(int position) {
                    return identifier.getId(position, CursorAdapper.this.getCursor());
                }
            });
        } else {
            Log.w("Adapper", "CursorAdapper initialized without Bidentifier, selection functionality will not be available");
        }
    }

    @Override
    protected Set<Integer> getViewTypes() {
        return provider.getViewTypes();
    }

    @Override
    public Object getItem(int position) {
        if (cursor != null) {
            cursor.moveToPosition(position);
            return miniOrm.getObject(cursor);
        }
        return null;
    }

    @Override
    public boolean isModel(int position) {
        return true;
    }

    @Override
    public Model getModel(int position) {
        return (Model) getItem(position);
    }

    @Override
    public int getItemViewType(int position) {
        return provider.getViewType(getModel(position));
    }

    @Override
    public TypedViewHolder<? super Model, ? extends GenericView> onCreateViewHolder(ViewGroup parent, int viewType) {
        return provider.create(LayoutInflater.from(parent.getContext()), parent, viewType);
    }

    @Override
    public void onBindViewHolder(TypedViewHolder<? super Model, ? extends GenericView> holder, int position) {
        holder.bind(getModel(position), position, getSelectionManager(position));
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public void changeCursor(Cursor cursor) {
        Cursor oldCursor = this.cursor;
        this.cursor = cursor;
        if (oldCursor != null) {
            oldCursor.close();
        }
        notifyDataSetChanged();
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new CursorFilter(this);
        }
        return filter;
    }

    @Nullable
    public Cursor getCursor() {
        return cursor;
    }

    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        this.filterQueryProvider = filterQueryProvider;
    }


    private static class CursorFilter extends Filter {

        private final CursorAdapper adapper;

        public CursorFilter(CursorAdapper adapper) {
            this.adapper = adapper;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Cursor cursor = adapper.runQueryOnBackgroundThread(constraint);

            FilterResults results = new FilterResults();
            if (cursor != null) {
                results.count = cursor.getCount();
                results.values = cursor;
            } else {
                results.count = 0;
                results.values = null;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Cursor oldCursor = adapper.getCursor();

            if (results.values != null && results.values != oldCursor) {
                adapper.changeCursor((Cursor) results.values);
            }
        }
    }

    private Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (filterQueryProvider != null) {
            return filterQueryProvider.runQuery(constraint);
        }

        return cursor;
    }

    /**
     * A class to which an instance of {@link com.scopely.adapper.interfaces.Identifier} will delegate, passing an additional {@link Cursor}
     */
    public interface CursorIdentifier {
        long getId(int position, Cursor cursor);
    }

    /**
     * A class to which an instance of {@link com.scopely.adapper.interfaces.Lookup} will delegate, passing an additional {@link MiniOrm}
     */
    public interface CursorLookup<T> {
        Set<T> getModels(Set<Long> ids, MiniOrm<T> miniOrm);
    }

    /**
     * A {@link CursorIdentifier} implementation that returns the value of a given column as an ID
     */
    public static class ColumnIdentifier implements CursorIdentifier {
        private final String columnName;

        public ColumnIdentifier(String columnName) {
            this.columnName = columnName;
        }

        @Override
        public long getId(int position, Cursor cursor) {
            if(cursor != null) {
                cursor.moveToPosition(position);
                return cursor.getLong(cursor.getColumnIndex(columnName));
            } else {
                return NO_ID;
            }
        }
    }

    /**
     * A {@link CursorLookup} implementation that searches for ids in a given column of a given {@link SQLiteDatabase}
     * @param <T> The type of the object represented by a row in the database
     */
    public static class ColumnLookup<T> implements CursorLookup<T> {
        private final String columnName;
        private final String tableName;
        private final SQLiteDatabase db;

        public ColumnLookup(SQLiteDatabase db, String tableName, String columnName) {
            this.columnName = columnName;
            this.tableName = tableName;
            this.db = db;
        }

        @Override
        public Set<T> getModels(Set<Long> ids, MiniOrm<T> miniOrm) {
            if(ids.isEmpty()) return Collections.EMPTY_SET;
            String[] params = new String[ids.size()];
            int count = 0;
            for(Long id : ids) {
                params[count] = id.toString();
                count++;
            }
            Cursor results = db.query(tableName, null, columnName + " IN (" + TextUtils.join(",", Collections.nCopies(ids.size(), "?")) + ")", params, null, null, null);
            int resultsCount = results.getCount();
            Set<T> set = SetUtils.newSet(resultsCount);
            for(int i = 0; i < resultsCount; i++) {
                results.moveToPosition(i);
                set.add(miniOrm.getObject(results));
            }
            return set;
        }

    }
}
