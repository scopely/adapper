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

package com.scopely.adapper.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.scopely.adapper.interfaces.Reorderable.NOT_PRESENT;

public class ListUtils {

    /**
     *
     * Takes two lists and returns the items that are present in the second list but not the first
     *
     * @return returns a SparseBooleanArray where the key is the index of the item in the second list, and the value is whether the object at that index has been inserted
     */
    public static <Item> SparseBooleanArray getInsertions(@Nullable List<? extends Item> oldList, @NonNull List<? extends Item> newList){
        SparseBooleanArray list = new SparseBooleanArray();
        if(oldList != null) {
            int size = newList.size();
            for (int newIndex = 0; newIndex < size; newIndex++) {
                Item itemFromNewList = newList.get(newIndex);
                if (oldList.indexOf(itemFromNewList) == NOT_PRESENT) {
                    list.put(newIndex, true);
                }
            }
        }
        return list;
    }

    /**
     *
     * Takes two lists and returns the items that are present in the first list but not the second
     *
     * @return returns a SparseBooleanArray where the key is the index of the item in the first list, and the value is whether the object at that index has been deleted
     */

    public static <Item> SparseBooleanArray getDeletions(@Nullable List<? extends Item> oldList, @NonNull List<? extends Item> newList) {
        SparseBooleanArray list = new SparseBooleanArray();
        if(oldList != null) {
            int size = oldList.size();
            for (int oldIndex = 0; oldIndex < size; oldIndex++) {
                Item itemFromOldList = oldList.get(oldIndex);
                if (newList.indexOf(itemFromOldList) == NOT_PRESENT) {
                    list.put(oldIndex, true);
                }
            }
        }
        return list;
    }

    /**
     *
     * Takes two lists and returns the items that are determined to have shifted their relative position from the first list to the second.
     *
     * @return a SparseIntArray where the key is the index of the item in the second list, and the value is the index of that item in the first list.
     */

    public static <Item> SparseIntArray getReorderings(List<? extends Item> oldList, List<? extends Item> newList){
        List<Pair<Integer, Integer>> moveList = calculateMoves(oldList, newList);
        List<Integer> netMoveScores = calculateNetMoveScores(moveList);
        SparseIntArray reorderings = new SparseIntArray();
        while(extractReordering(moveList, netMoveScores, reorderings));
        return reorderings;
    }

    /**
     *
     * Takes two lists and returns a list of pairs, where the first element is the index of a given object in the first list, and the second element is the index of that same element in the second list
     *
     * @return a List of Pairs where first = index in {@param oldList} and second = index in {@param newList}.
     * The index of the list of pairs itself will correspond to the index of {@param newList} except for items which were deleted, which are appended to the end of the list
     */

    private static <Item> List<Pair<Integer, Integer>> calculateMoves(List<? extends Item> oldList, List<? extends Item> newList) {
        List<Pair<Integer, Integer>> list = new ArrayList<Pair<Integer, Integer>>();
        if (oldList != null) {
            for (int newPostion = 0; newPostion < newList.size(); newPostion++) {
                int oldPosition = oldList.indexOf(newList.get(newPostion));
                if(oldPosition < 0) oldPosition = NOT_PRESENT;
                list.add(newPostion, new Pair<>(oldPosition, newPostion));
            }
            for(int oldPosition = 0; oldPosition < oldList.size(); oldPosition++) {
                if(!newList.contains(oldList.get(oldPosition))) {
                    list.add(new Pair<>(oldPosition, NOT_PRESENT));
                }
            }
        }
        return list;
    }

    /**
     *
     * Takes a list of moves and calculates the distance that moves represents (as well as the direction, negative for decreasing index, positive for increasing index).
     * Uses a value of 0 for objects that were inserted, and does not include objects that were deleted.
     *
     * @param moveList A list of moves. Assumes that all deletions are at the end of the list.
     *
     * @return a list of net move scores
     */

    private static List<Integer> calculateNetMoveScores(List<Pair<Integer, Integer>> moveList) {
        List<Integer> list = new ArrayList<>();
        for(Pair<Integer, Integer> pair : moveList) {
            if(pair.first >= 0 && pair.second >= 0){
                list.add(pair.second - pair.first);
            } else if (pair.second >= 0) {
                list.add(0);
            }
        }
        cleanNetMoveList(list, moveList);
        return list;
    }

    /**
     *
     * Takes a list of moves and corresponding move scores, and extracts a reordering corresponding to the highest net move score in the list.
     * Once extracted, it sets the net move score to zero, and then corrects for the cascade of the move.
     * Correction is accomplished by either adding or subtracting 1 to the net move score of all indices between the move's start and finish positions.
     *
     * @return true iff a reordering has been found
     */
    private static boolean extractReordering(List<Pair<Integer, Integer>> moveList, List<Integer> netMoveScores, SparseIntArray reorderings) {
        int index = indexOfHighestNetMove(netMoveScores);
        if(index < 0) return false;

        int score = netMoveScores.get(index);
        Pair<Integer, Integer> move = moveList.get(index);
        reorderings.put(move.second, move.first);
        netMoveScores.set(index, 0);
        if(score > 0){
            for(int i = index - score; i < index; i++){
                if(moveList.get(i).first != NOT_PRESENT && moveList.get(i).second != NOT_PRESENT){
                    netMoveScores.set(i, netMoveScores.get(i) + 1);
                }
            }
        } else if (score < 0) {
            for(int i = index - score; i > index; i--){
                if(moveList.get(i).first != NOT_PRESENT && moveList.get(i).second != NOT_PRESENT){
                    netMoveScores.set(i, netMoveScores.get(i) - 1);
                }
            }
        } else {
            //we should not be here
        }
        return true;
    }

    private static String generateString(List<Pair<Integer, Integer>> moveList) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(Pair<Integer, Integer> pair : moveList) {
            builder.append(String.format(Locale.US, "(%d,%d)", pair.first, pair.second));
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     *
     * Goes through a moveList and a corresponding list of net move scores, and adjusts the net move scores to account for insertions and deletions
     *
     * @param netMoveScores a list of net move scores
     * @param moveList a list of moves
     */

    private static void cleanNetMoveList(List<Integer> netMoveScores, List<Pair<Integer, Integer>> moveList) {
        for(int i = 0; i < moveList.size(); i++) {
            Pair<Integer, Integer> move = moveList.get(i);
            if(move.first < 0){
                for(int j = move.second + 1; j < netMoveScores.size(); j++) {
                    if(moveList.get(j).first != NOT_PRESENT){
                        netMoveScores.set(j, netMoveScores.get(j) - 1);
                    }
                }
            } else if (move.second < 0) {
                for(int j : getNewIndicesOfOldIndicesAboveIndex(move.first, moveList)) {
                    if(moveList.get(j).second != NOT_PRESENT){
                        netMoveScores.set(j, netMoveScores.get(j) + 1);
                    }
                }
            }
        }
    }

    /**
     *
     * Finds the index of the highest net move score
     *
     * @param netMoveScores A list of net move scores
     * @return the index of the highest absolute value in the list
     */

    private static int indexOfHighestNetMove(List<Integer> netMoveScores) {
        int index = -1;
        int max = 0;
        for (int i = 0; i < netMoveScores.size(); i++) {
            int value = Math.abs(netMoveScores.get(i));
            if(value > max) {
                index = i;
                max = value;
            }
        }
        return index;
    }

    /**
     *
     * Takes a list of moves between two lists, and returns the indices, in the new list, of all the old indices above a given index.
     *
     * @param index the value in the old list above which the corresponding indices in the new list will be found
     * @param moveList a list of moves between two lists (where each move is a pair where first = index in old list, second = index in new list)
     *
     * @return a list of indices in the new list for which their corresponding index in the old list is above {@param index}
     */

    private static List<Integer> getNewIndicesOfOldIndicesAboveIndex(Integer index, List<Pair<Integer, Integer>> moveList) {
        List<Integer> list = new ArrayList<>();
        for(Pair<Integer, Integer> move : moveList) {
            if(move.first > index && move.second >= 0) {
                list.add(move.second);
            }
        }
        return list;
    }
}
