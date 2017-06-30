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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.scopely.adapper.interfaces.SelectionManager;

public abstract class TypedViewHolder<Model, GenericView extends View> extends RecyclerView.ViewHolder {
    public TypedViewHolder(GenericView itemView) {
        super(itemView);
    }

    @SuppressWarnings("unchecked")
    public GenericView getView() {
        return (GenericView) itemView;
    }

    public void bind(Model model, int position, SelectionManager selectionManager) {
        bind(getView(), model, position, selectionManager);
    }

    protected abstract void bind(GenericView view, Model model, int position, @Nullable SelectionManager selectionManager);

}
