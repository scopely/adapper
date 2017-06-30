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

package com.scopely.adapper.utils;

import android.util.Log;
import android.widget.Filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * Filter that is composed of multiple other filters. Only completes when all of its children have completed.
 */
public class CompositeFilter extends Filter {

    Filter[] filters;
    private Method performFiltering;
    private Method publishResults;

    public CompositeFilter(Filter... filters) {
        this.filters = filters;
        try {
            //Reflection is unfortuante. Seemingly no other way to access a filter's logic synchronously.
            performFiltering = Filter.class.getDeclaredMethod("performFiltering", CharSequence.class);
            performFiltering.setAccessible(true);

            publishResults = Filter.class.getDeclaredMethod("publishResults", CharSequence.class, FilterResults.class);
            publishResults.setAccessible(true);

        } catch (NoSuchMethodException e) {
            Log.e("adapper", "FilterUtils error", e);
        }
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        if(performFiltering != null){
            try {
                int count = 0;
                Map<Filter, FilterResults> map = new HashMap<>();

                for (Filter filter : filters) {
                    if(filter != null) {
                        FilterResults results = (FilterResults) performFiltering.invoke(filter, constraint);
                        count += results.count;
                        map.put(filter, results);
                    }
                }

                FilterResults results = new FilterResults();
                results.count = count;
                results.values = map;
                return results;
            } catch (IllegalAccessException e) {
                Log.e("adapper", "FilterUtils error", e);
                return new FilterResults();
            } catch (InvocationTargetException e) {
                Log.e("adapper", "FilterUtils error", e);
                return new FilterResults();
            }
        } else {
            return new FilterResults();
        }
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        @SuppressWarnings("unchecked")
        Map<Filter, FilterResults> map = (Map<Filter, FilterResults>) results.values;
        if(map != null && publishResults != null) {
            try {
                for (Filter filter : filters) {
                    if(filter != null){
                        publishResults.invoke(filter, constraint, map.get(filter));
                    }
                }
            } catch (IllegalAccessException e) {
                Log.e("adapper", "FilterUtils error", e);
            } catch (InvocationTargetException e) {
                Log.e("adapper", "FilterUtils error", e);
            }
        }
    }
}
