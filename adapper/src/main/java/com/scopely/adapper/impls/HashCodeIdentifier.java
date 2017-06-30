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
import com.scopely.adapper.interfaces.Identifier;

/**
 * An Identifier that returns the hashcode of the object at a position within an Adapper as its ID
 */
public class HashCodeIdentifier implements Identifier {
    private final BaseAdapper adapper;

    public HashCodeIdentifier(BaseAdapper adapper) {
        this.adapper = adapper;
    }

    @Override
    public long getId(int position) {
        Object obj = adapper.getItem(position);
        return obj != null ? obj.hashCode() : RecyclerView.NO_ID;
    }
}
