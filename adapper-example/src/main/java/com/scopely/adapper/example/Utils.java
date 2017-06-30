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

import com.scopely.adapper.example.objects.NameAndMood;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<NameAndMood> generateNameAndMoodList(int size) {
        List<NameAndMood> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            String name = "Unique Name #" + i;
            NameAndMood.Mood[] moods = NameAndMood.Mood.values();
            NameAndMood.Mood mood = moods[i % moods.length];
            list.add(new NameAndMood(mood, name));
        }
        return list;
    }

    public static List<String> generateStringList(int size) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            list.add("Item #" + i);
        }
        return list;
    }
}
