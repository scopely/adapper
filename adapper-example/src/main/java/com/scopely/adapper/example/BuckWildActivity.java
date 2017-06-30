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

import android.os.Bundle;

import com.scopely.adapper.example.fragments.BuckWildFragment;

import java.util.Arrays;


public class BuckWildActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fragments = Arrays.asList(
                new BuckWildFragment()
        );
        titles = Arrays.asList(
                title(R.string.title_section_buck_wild)
        );
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.menu_navigation_buck_wild;
    }
}
