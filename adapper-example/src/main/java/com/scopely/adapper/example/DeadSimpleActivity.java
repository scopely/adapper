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

package com.scopely.adapper.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.scopely.adapper.adapters.ListAdapper;
import com.scopely.adapper.impls.ViewProviderImpl;
import com.scopely.adapper.interfaces.SelectionManager;
import com.scopely.adapper.interfaces.ViewProvider;

import java.util.Arrays;
import java.util.List;


public class DeadSimpleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> list = Arrays.asList("First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth",
                "Eleventh", "Twelfth", "Thirteenth", "Fourteenth", "Fifteenth", "Sixteenth", "Seventeenth", "Eighteenth", "Nineteenth", "Twentieth");

        RecyclerView view = new RecyclerView(this);
        view.setLayoutManager(new LinearLayoutManager(this));
        setContentView(view);

        ViewProvider<String, TextView> provider = new ViewProviderImpl<String, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            protected void bind(TextView view, String s, int position, @Nullable SelectionManager selectionManager) {
                view.setText(s);
            }

            @Override
            public int getViewType(String s) {
                return android.R.layout.simple_list_item_1;
            }
        };

        RecyclerView.Adapter adapter = new ListAdapper<>(list, provider);
        view.setAdapter(adapter);
    }




    //-----------------------------------------Navigation-----------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_base, menu);
        menu.findItem(R.id.menu_navigation_dead_simple).setVisible(false);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_navigation_adapters) {
            startActivity(new Intent(this, AdaptersActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            return true;
        } else if (item.getItemId() == R.id.menu_navigation_selectors){
            startActivity(new Intent(this, SelectorsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            return true;
        } else if (item.getItemId() == R.id.menu_navigation_buck_wild){
            startActivity(new Intent(this, BuckWildActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            return true;
        } else
            return super.onMenuItemSelected(featureId, item);
    }
    //---------------------------------------End Navigation---------------------------------------
}
