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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A List<U> wherein each object of type U at index i is the output of a function that evaluates the object of type T at index i in an underlying List<T>.
 */
public class FunctionList<Input, Output> implements List<Output> {

    Function<Input, Output> function;
    List<Input> list;

    public FunctionList(List<Input> list, Function<Input, Output> function) {
        this.function = function;
        this.list = list;
    }

    @Override
    public void add(int location, Output object) {
        throw new FunctionException();
    }

    @Override
    public boolean add(Output object) {
        throw new FunctionException();
    }

    @Override
    public boolean addAll(int location, @NonNull Collection<? extends Output> collection) {
        throw new FunctionException();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends Output> collection) {
        throw new FunctionException();
    }

    @Override
    public void clear() {
        getList().clear();
    }

    @Override
    public boolean contains(Object object) {
        return indexOf(object) >= 0;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        for(Object obj : collection) {
            if(!contains(obj)) return false;
        }
        return true;
    }

    @Override
    public Output get(int location) {
        return function.evaluate(getList().get(location));
    }

    @Override
    public int indexOf(Object object) {
        for(int i = 0; i < size(); i++) {
            if(object.equals(get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return getList().isEmpty();
    }

    @NonNull
    @Override
    public Iterator<Output> iterator() {
        final Iterator<Input> internalIterator = getList().iterator();
        return new Iterator<Output>() {
            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }

            @Override
            public Output next() {
                return function.evaluate(internalIterator.next());
            }

            @Override
            public void remove() {
                internalIterator.remove();
            }
        };
    }

    @Override
    public int lastIndexOf(Object object) {
        for(int i = size() - 1; i > 0; i--) {
            if(object.equals(get(i))) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ListIterator<Output> listIterator() {
        return listIterator(0);
    }

    @NonNull
    @Override
    public ListIterator<Output> listIterator(int location) {
        final ListIterator<Input> internalIterator = getList().listIterator(location);
        return new ListIterator<Output>() {
            @Override
            public void add(Output object) {
                throw new FunctionException();
            }

            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return internalIterator.hasPrevious();
            }

            @Override
            public Output next() {
                return function.evaluate(internalIterator.next());
            }

            @Override
            public int nextIndex() {
                return internalIterator.nextIndex();
            }

            @Override
            public Output previous() {
                return function.evaluate(internalIterator.previous());
            }

            @Override
            public int previousIndex() {
                return internalIterator.previousIndex();
            }

            @Override
            public void remove() {
                internalIterator.remove();
            }

            @Override
            public void set(Output object) {
                throw new FunctionException();
            }
        };
    }

    @Override
    public Output remove(int location) {
        return function.evaluate(getList().remove(location));
    }

    @Override
    public boolean remove(Object object) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public Output set(int location, Output object) {
        throw new FunctionException();
    }

    @Override
    public int size() {
        return getList().size();
    }

    @NonNull
    @Override
    public List<Output> subList(int start, int end) {
        return new FunctionList<>(getList().subList(start, end), function);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] contents) {
        int s = size();
        if (contents.length < s) {
            @SuppressWarnings("unchecked") T[] newArray
                    = (T[]) Array.newInstance(contents.getClass().getComponentType(), s);
            contents = newArray;
        }
        for(int i = 0; i < s; i++) {
            contents[i] = (T) get(i);
        }
        if (contents.length > s) {
            contents[s] = null;
        }
        return contents;
    }

    protected List<Input> getList() {
        return list;
    }

    public interface Function<T, U> {
        U evaluate(T input);
    }
    
    private static class FunctionException extends UnsupportedOperationException {
        private FunctionException() {
            super("Operation requires determining the input of a function from the output, which is not defined");
        }
    }
}
