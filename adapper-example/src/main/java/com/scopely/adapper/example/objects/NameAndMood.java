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

package com.scopely.adapper.example.objects;

import android.support.annotation.DrawableRes;

import com.scopely.adapper.example.R;

import java.util.Random;

public class NameAndMood {
    public final Mood mood;
    public final String name;

    public NameAndMood(Mood mood, String name) {
        this.mood = mood;
        this.name = name;
    }

    public enum Mood {
        HAPPY(R.drawable.happy),
        NEUTRAL(R.drawable.neutral),
        SAD(R.drawable.sad)
        ;

        public int imageId;

        Mood(@DrawableRes int resId) {
            this.imageId = resId;
        }

        public static Mood random() {
            return Mood.values()[new Random().nextInt(3)];
        }
    }

    @Override
    public String toString() {
        return mood.toString() + " " + name;
    }
}
