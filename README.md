# Adapper
Adapper is a library that simplifies and streamlines the task of mapping collections of data to views that display that data.
## Features
Adapper allows you to create Adapters for use with Android's `RecyclerView` with minimal effort. Implementation requires only a small handful of constructors and methods that define the dataset and how it binds to an individual view. Adapper handles the rest, including: mapping position within the dataset to position within the ```RecyclerView```; tracking additions, subtractions, and reorderings; sorting and grouping the data; driving a single ```ReyclerView``` with multiple adapters; tracking selections; and more.
## Gradle
```groovy
compile 'com.scopely:adapper:1.0.0'
```
## Setup
Adapper requires no setup or initialization before use. Adapters created with Adapper can be instantiated when needed and used on regular ```RecyclerView```s without modification.
## Usage
Adapper is fairly straightforward to use. ```ReyclerView```s can be created in Java or in XML as normal. The user can then create an instance of an Adapper and call```RecyclerView#setAdapter(adapper)```
### Adappers
```BaseAdapper``` is the core class of the Adapper library. It directly extends ```RecyclerView.Adapter``` and is itself subclassed by several different Adappers depending on the type of data and presentation desired.
#### ListAdapper
```ListAdapper``` is the most basic and straightforward of the Adappers. It is parameterized in two variables: ```Model``` which is the class type of the data, and ```GenericView``` which is the class type of the ```View``` the adapter will provide to the ```RecyclerView```. Its constructor requires only two parameters: a ```List<Model>``` and a ```ViewProvider<Model, GenericView>```.
#### ViewProvider
The ```ViewProvider``` interface is required as a parameter by several of the Adappers. Unless you have specific reason to roll your own, it is recommended you use the ```ViewProviderImpl``` implementation. Under the hood the ```ViewProviderImpl``` is managing inflation of layouts, creation of ```ViewHolder```s, and communicating to the Adapter how to recycle its view types. Creating a new instance of ```ViewProviderImpl``` is as simple as passing all possible layouts into its constructor:
and then implementing two methods:
```java
ViewProvider<Model, GenericView> provider = new ViewProviderImpl<Model, GenericView>(R.layout.layout_1, R.layout.layout_2) {
    @Override
    public int getViewType(Model model) {
        if (some_condition) {
            return R.layout.layout_1;
        } else {
            return R.layout.layout_2;
        }
    }

    @Override
    protected void bind(GenericView view, Model model, int position, SelectionManager* selectionManager) {
        view.setModel(model);
    }
};
```
The first method tells the Adapter which layout to provide for a given instance of `Model` and the latter binds the data in that instance to an instance of the layout.

*You may have noticed the class `SelectionManager`. This is covered in the Selection section down below

#### GroupableAdapper
A common use case for visual lists of data is to sort them into sections and provide a header that accompanies each section. `GroupableAdapper` handles this case and is nearly as easy to instantiate as `ListAdapper`. Like `ListAdapper` it takes a `List<Model>` and a `ViewProvider<Model, GenericView>`, but it also takes a `GroupComparator<Model, Group>` and a `ViewProvider<Group, GroupView>`. The former tells the `GroupAdapper` how to sort the list of data and divide it into sections, and the latter provides the views for the section headers.

#### GroupComparator
`GroupComparator`, like `ViewProvider`, has a recommended implementation `GroupComparatorImpl`. It requires three methods be implemented:
```java
@Override
public Group getGroup(Model item) {
    return item.getGroup();
}
```
```java
@Override
protected int groupCompare(Group lhs, Group rhs) {
    return lhs.compareTo(rhs);
}
```
```java
@Override
protected int itemCompare(Model lhs, Model rhs) {
    return lhs.compareTo(rhs);
}
```

The first simply returns the section or group in which the provided instance of Model belongs. The latter two methods follow the contract of `Comparator#compare()`, returning 0 for equality, < 0 for comparisons where lhs is less than rhs, and > 0 otherwise.

#### CursorAdapper
`CursorAdapper` works much like `ListAdapper`. Instead of providing a `List<Model>` however, the implementer provides a Cursor pointed at a database table as well as a single function `MiniOrm<Model>` object that can generate a `Model` object out of an entry in a query result. `CursorAdapper` itself will take care of making sure the `Cursor` is positioned correctly before it is passed into `MiniOrm#getObject()`

#### MiniOrm
```java
MiniOrm<String> miniOrm = new MiniOrm<String>() {
@Override
public String getObject(Cursor c) {
	return c.getString(c.getColumnIndex("name"))
};
```
#### RecursiveAdapper
RecursiveAdapper allows you to place other Adappers inside it. It handles all the internal delegating of calls to the appropriate child given the position in the RecyclerView, including adjusting the position it passes to the child Adapper. The child Adapper requires no knowledge of the fact that it is being wrapped and delegated to, and thus any Adapper can be placed into a RecursiveAdapper, allowing for lists to be built in a decoupled and modular way.
#### SingleViewAdapper
This Adapper wraps a single, pre-inflated, view in the trappings of `BaseAdapper`. The common use case for this Adapper is to place a semi-complex view hierarchy in between two other Adappers in a parent `RecursiveAdapper`.
### Filters
Many of the Adappers implement `Filterable`, but require a small bit of additional code to make operable. `ListAdapper` and `GroupableAdapper` can take a `FilterFunction` object, and CursorAdapper can take a `FilterQueryProvider` object to complete the circuit* as seen below:
```java
adapper.setFilterFunction(new FilterFunction<String>() {
    @Override
    public boolean filter(String item, @Nullable CharSequence constraint) {
        return constraint == null || item.contains(constraint);
    }
});
```
```java
adapper.setFilterQueryProvider(new FilterQueryProvider() {
    @Override
    public Cursor runQuery(CharSequence constraint) {
        if(constraint != null && constraint.length() > 0){
            return db.query("name", null, "name LIKE '%"+constraint.toString()+"%'", null, null, null, null);
        } else {
             return db.query("name", null, null, null, null, null, null);
        }
    }
});
```
*`RecursiveAdapper` does no filtering of its own, but will return a composite `Filter` built from the `Filter`s of its children if `getFilter()` is called on it

### SelectionManager
One thing that ListView came with out of the box that `RecyclerView` didn't carry over was the concept of tracking clicks and selections within the list. RecyclerView is cleaner in this sense, as there is no "magic behavior" with how the view handles touch events, but it does require that selections be managed separately from the RecyclerView.
The most appropriate place to track selections is within the Adapter itself, and Adapper provides `BaseAdapper#setSelectionManager(SelectionManager selectionManager)` in order to enable this functionality. This SelectionManager will be passed through to ViewProvider's onBind method, where it can be hooked in to the provided view:
```java
@Override
protected void bind(GenericView view, Model model, int position, SelectionManager selectionManager) {
    view.setModel(model);
    view.setChecked(selectionManager.isItemSelected(position));
    view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectionManager.selectItem(position, !selectionManager.isItemSelected(position));
        }
    });
}
```
`SelectionManager` also includes `void clear()` and `List<Model> getSelections()` methods, both of which can be accessed from `BaseAdapper` (the call on `BaseAdapper` will delegate to the `BaseAdapper`'s `SelectionManager`). A `null` `SelectionManager` in `BaseAdapper` will disable all selection functionality.
There are two provided `SelectionManager` implementations:
#### RadioSelectManager
`RadioSelectManager` allows for a single selection at a time. Selecting a new item will clear the previous selection.
#### MultiSelectManager
`MultiSelectManager` allows for multiple items to be selected, up to a provided cap. If the user tries to select more than the set maximum, the `SelectionManager#setSelected(int position, boolean selected)` method will return false, and the `MultiSelectManager#onMaximumExceeded(int maximumSelectable)` method will be called.

### Bidentifier
`SelectionManagers` rely on `Adapter#getItemId(int position)` to track selections. A `Bidentifier` is a simple bi-directional identifier, and extends `Identifier` and `Lookup` (one for each direction). Most Adappers will instantiate a `Bidentifier` by default (either a naive implementation, or one that delegates to the `Bidentifier` of the appropriate child in the case of `RecursiveAdapper` and `GroupableAdapper`), but default implementations can be overriden by calling `BaseAdapper#setBidentifier(Bidentifier bidentifier)`. CursorAdapper in particular lacks a default implementation, and requires a `CursorIdentifier` and a `CursorLookup` in its constructor, which it will then wrap with a `Bidentifier` internally.
#### Identifier
`Identifier` maps a position in a list to the ID of the item presently at that position. It has a naive implementation, `HashCodeIdentifier`, which simply returns the hashcode of the object at `Adapter#getItem(int position)`
#### Lookup
`Lookup` maps an ID to the object for which that ID applies. The API exposes a Set -> Set mapping instead of a singular ID -> Object mapping to enable more efficient bulk lookups such as querying from a database. It has a naive implementation `NaiveLookup` that simply iterates over an adapter and does a comparison with `getItemId(int position)`

### GridLayouts
`RecyclerView` determines how to layout its child views based on its LayoutManager. By default `RecyclerView`s will use a `LinearLayoutManager`, but the support library also includes `GridLayoutManager` for creating layouts that have multiple views per row, similar to the old `GridView`. `GridLayoutManager` takes an optional `SpanSizeLookup` object that allows you to specify how many "cells" in the grid a given view takes up. Adapper provides classes that extend `SpanSizeLookup` that make the task of creating full width headers and other related tasks simpler.

```java
InvertedSpanSizeLookup spanSizeLookup = new InvertedSpanSizeLookup(1, 5, 10) {
    @Override
    public int getItemsPerRow(int position) {
        if (adapter.isGroup(position)) {
            return 1;
        } else {
            if (some_condition) {
                return 5;
            } else {
                return 10;
            }
        }
    }
};
GridLayoutManager manager = new GridLayoutManager(context, spanSizeLookup.getRequiredSpans());
manager.setSpanSizeLookup(spanSizeLookup);
recyclerView.setLayoutManager(manager);
```
### Tracking Data Changes
The old `BaseAdapter`, which paired with `ListView`, contained `BaseAdapter#notifyDatasetChanged()`. `RecyclerView`'s `Adapter` has maintained this method, but has also added a number of methods to notify the `Adapter` of insertions, deletions, and movements within the dataset. These methods will trigger `RecyclerView`'s animation functions, whereas `notifyDatasetChanged()` will cause the `RecyclerView` to reflect the new state of the dataset with no transition animations. `BaseAdapper` includes an `update()` method that computes all of the insertions, deletions, and reorderings, and then calls the appropriate notification methods. It is as easy to use as the old `notifyDatesetChanged()` while keeping animation functions enabled.