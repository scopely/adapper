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

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.interfaces.ViewProvider;
import com.scopely.adapper.utils.SetUtils;

import java.util.Set;

public abstract class ViewProviderImpl<Model, GenericView extends View> implements ViewProvider<Model, GenericView> {
    protected final Set<Integer> layouts;

    protected ViewProviderImpl(@LayoutRes int... layouts) {
        this.layouts = SetUtils.newSet();
        for (int i : layouts) {
            this.layouts.add(i);
        }
    }

    @Override
    public Set<Integer> getViewTypes() {
        return layouts;
    }

    @Override
    public TypedViewHolder<Model, GenericView> create(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new TypedViewHolder<Model, GenericView>((GenericView) inflater.inflate(viewType, parent, false)) {
            @Override
            protected void bind(GenericView view, Model model, int position, SelectionManager selectionManager) {
                ViewProviderImpl.this.bind(view, model, position, selectionManager);
            }
        };
    }

    protected abstract void bind(GenericView view, Model model, int position, @Nullable SelectionManager selectionManager);

    @Override
    @LayoutRes
    public abstract int getViewType(Model model);
}
