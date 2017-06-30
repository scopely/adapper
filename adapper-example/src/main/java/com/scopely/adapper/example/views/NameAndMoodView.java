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

package com.scopely.adapper.example.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scopely.adapper.example.R;
import com.scopely.adapper.example.objects.NameAndMood;
import com.scopely.adapper.interfaces.Populatable;


public class NameAndMoodView extends LinearLayout implements Populatable<NameAndMood>, Checkable {

    public static final int COLOR = Color.parseColor("#8A57AB");
    public TextView textView;
    public ImageView imageView;
    private ImageView checkMark;
    private boolean checked;
    private boolean gridMode;

    public NameAndMoodView(Context context) {
        super(context);
    }

    public NameAndMoodView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NameAndMoodView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.textView = (TextView) findViewById(R.id.textView);
        this.imageView = (ImageView) findViewById(R.id.imageView);
        this.checkMark = (ImageView) findViewById(R.id.checkMark);
    }

    public void setGridMode(boolean gridMode) {
        this.gridMode = gridMode;
        setChecked(checked);
        if(gridMode) {
            textView.setVisibility(GONE);
        } else {
            textView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setModel(NameAndMood model) {
        textView.setText(model.name);
        imageView.setImageResource(model.mood.imageId);
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        if(!gridMode){
            checkMark.setVisibility(checked ? VISIBLE : INVISIBLE);
            setBackgroundColor(0);
        } else {
            checkMark.setVisibility(GONE);
            setBackgroundColor(checked ? COLOR : 0);
        }
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }
}
