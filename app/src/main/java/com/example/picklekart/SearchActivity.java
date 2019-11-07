package com.example.picklekart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private TextView textView;
    private RecyclerView recyclerView;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Search Results");

        textView = findViewById(R.id.textview);
        recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);


        final Adapter adapter = new Adapter(MainActivity.list, false);
        adapter.setFromSearch(true);
        if (getIntent().getStringExtra("textview").equals("VISIBLE")) {
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else if (getIntent().getStringExtra("textview").equals("GONE")) {

            String query = getIntent().getStringExtra("query");

            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.getFilter().filter(query);
        }
        recyclerView.setAdapter(adapter);

    }

    static class Adapter extends WishlistAdapter implements Filterable {

        private List<WishlistModel> originalList;

        public Adapter(List<WishlistModel> wishlistModelList, Boolean wishlist) {
            super(wishlistModelList, wishlist);
            originalList = wishlistModelList;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {


                    FilterResults results = new FilterResults();
                    List<WishlistModel> filteredList = new ArrayList<>();
                    final String[] tags = charSequence.toString().toLowerCase().split(" ");

                    for (WishlistModel model : originalList) {
                        ArrayList<String> presentTags = new ArrayList<>();
                        for (String tag : tags) {
                            if (model.getTags().contains(tag)) {
                                presentTags.add(tag);
                            }
                        }

                        model.setTags(presentTags);
                    }


                    for (int i = tags.length; i > 0; i--) {
                        for (WishlistModel model : originalList) {
                            if (model.getTags().size() == i) {
                                filteredList.add(model);
                            }
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();

                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults results) {

                    if (results.count > 0) {
                        setWishlistModelList((List<WishlistModel>) results.values);

                    }
                    notifyDataSetChanged();
                }
            };
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);


    }
}
