package com.aliucord.themer.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliucord.themer.R;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    public static class Adapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View view) { super(view); }
        }

        public static class Item<T> {
            public String name;
            public T value;

            public Item(String name, T val) {
                this.name = name;
                value = val;
            }
        }

        private final List<Adapter.Item<T>> items;
        private List<Adapter.Item<T>> itemsFiltered;
        private final FragmentManager fragmentManager;
        private final Bind<T> bind;
        public Adapter(FragmentManager fm, List<Adapter.Item<T>> items, Bind<T> bind) {
            this.items = items;
            itemsFiltered = new ArrayList<>(items);
            fragmentManager = fm;
            this.bind = bind;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new Adapter.ViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            bind.onBindViewHolder((ViewGroup) holder.itemView, itemsFiltered.get(position), fragmentManager);
        }

        @Override
        public int getItemCount() {
            return itemsFiltered.size();
        }

        private final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Adapter.Item<T>> resultsList;
                if (constraint == null || constraint.equals("")) resultsList = items;
                else {
                    resultsList = new ArrayList<>();
                    for (Adapter.Item<T> item : items) {
                        if (item.name != null && item.name.toLowerCase().contains(constraint.toString().toLowerCase().trim()))
                            resultsList.add(item);
                    }
                }
                FilterResults results = new FilterResults();
                results.values = resultsList;
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemsFiltered = (List<Adapter.Item<T>>) results.values;
                notifyDataSetChanged();
            }
        };
        @Override
        public Filter getFilter() {
            return filter;
        }
    }

    public interface Bind<T> {
        void onBindViewHolder(ViewGroup group, Adapter.Item<T> item, FragmentManager fragmentManager);
    }

    private Adapter<?> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        if (adapter != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            adapter.getFilter().filter(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return true; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.getFilter().filter(newText);
                return true;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    public void setAdapter(Adapter<?> adapter) {
        this.adapter = adapter;
        RecyclerView rv = findViewById(R.id.colors_list);
        rv.setAdapter(adapter);
    }
}
