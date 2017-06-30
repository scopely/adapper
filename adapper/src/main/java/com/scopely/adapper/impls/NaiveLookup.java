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

package com.scopely.adapper.impls;

import android.support.v7.widget.RecyclerView;

import com.scopely.adapper.adapters.BaseAdapper;
import com.scopely.adapper.interfaces.Lookup;
import com.scopely.adapper.utils.SetUtils;

import java.util.Set;

/**
 * A Lookup class that iterates over the elements in an Adapper and compares the IDs until it finds a match
 * @param <T>
 */
public class NaiveLookup<T> implements Lookup<T> {
    private final BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper;

    public NaiveLookup(BaseAdapper<? extends T, ? extends RecyclerView.ViewHolder> adapper) {
        this.adapper = adapper;
    }

    @Override
    public Set<T> getModels(Set<Long> ids) {
        Set<T> list = SetUtils.newSet();
        for(int i = 0; i < adapper.getItemCount(); i++) {
            if(ids.contains(adapper.getItemId(i))) {
                list.add(adapper.getModel(i));
            }
        }
        return list;
    }
}
