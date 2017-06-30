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

package com.scopely.adapper.interfaces;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scopely.adapper.impls.TypedViewHolder;

import java.util.Set;

public interface ViewProvider<Model, GenericView extends View>  {
    /**
     * Return the id of the layout to use for the provided item.
     * Items that return the same id can have their views recycled between them.
     * It is recommended that you use the R value of the layout resource from which the view is inflated
     */
    int getViewType(Model model);

    /**
     * Return a set of all the layout ids this provider is capable of providing.
     */
    Set<Integer> getViewTypes();

    /**
     *
     * Returns an instance of GenericView appropriate for the provided Item.
     *
     * @param inflater a layout inflater for the current context. Generally provided by the adapter.
     * @param parent the parent view into which the provided view will be placed. Generally a ListView or similar.
     * @return an instance of {@link TypedViewHolder}
     */
    TypedViewHolder<Model, GenericView> create(LayoutInflater inflater, ViewGroup parent, int viewType);

}
