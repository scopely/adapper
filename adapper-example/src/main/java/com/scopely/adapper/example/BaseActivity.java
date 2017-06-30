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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;

public abstract class BaseActivity extends Activity {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    List<? extends Fragment> fragments;
    List<? extends CharSequence> titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TitlePageIndicator mViewPagerIndicator = (TitlePageIndicator) findViewById(R.id.titles);
        mViewPagerIndicator.setViewPager(mViewPager);
        mViewPagerIndicator.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
        mViewPagerIndicator.setSelectedColor(getResources().getColor(android.R.color.holo_blue_bright));
    }


    /**
     * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
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
        } else if (item.getItemId() == R.id.menu_navigation_dead_simple){
            startActivity(new Intent(this, DeadSimpleActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            return true;
        } else
            return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_base, menu);
        menu.findItem(getNavigationItemId()).setVisible(false);
        return true;
    }

    protected CharSequence title(int id) {
        return getString(id);
    }

    protected abstract int getNavigationItemId();
}
