package com.example.picklekart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.picklekart.DBqueries.lists;
import static com.example.picklekart.DBqueries.loadFragmentData;
import static com.example.picklekart.DBqueries.loadedCategoriesNames;
import static com.example.picklekart.MainActivity.ids;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;
    private HomePageAdapter adapter;
    private List<HomePageModel> homePageModelFakeList = new ArrayList<>();
    private MenuItem searchItem;
    private Dialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);


        /////////// Loading Dialog


        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));

        //////////// Loading Dialog

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = getIntent().getStringExtra("CategoryName");
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categoryRecyclerView = findViewById(R.id.category_recyclerview);


        /////////////////// Home Page fake list

        List<SliderModel> sliderModelFakeList = new ArrayList<>();
        sliderModelFakeList.add(new SliderModel("null", "#ffffff"));
        sliderModelFakeList.add(new SliderModel("null", "#ffffff"));
        sliderModelFakeList.add(new SliderModel("null", "#ffffff"));
        sliderModelFakeList.add(new SliderModel("null", "#ffffff"));
        sliderModelFakeList.add(new SliderModel("null", "#ffffff"));

        List<HorizontalProductScrollModel> horizontalProductScrollModelFakeList = new ArrayList<>();
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));

        homePageModelFakeList.add(new HomePageModel(0, sliderModelFakeList));
        homePageModelFakeList.add(new HomePageModel(1, "", "#ffffff"));
        homePageModelFakeList.add(new HomePageModel(2, "", "#ffffff", horizontalProductScrollModelFakeList, new ArrayList<WishlistModel>()));
        homePageModelFakeList.add(new HomePageModel(3, "", "#ffffff", horizontalProductScrollModelFakeList));

        //////////////////// Home Page fake list


        LinearLayoutManager testingLayoutManager = new LinearLayoutManager(this);
        testingLayoutManager.setOrientation(RecyclerView.VERTICAL);
        categoryRecyclerView.setLayoutManager(testingLayoutManager);
        adapter = new HomePageAdapter(homePageModelFakeList);
        int listPosition = 0;
        for (int x = 0; x < loadedCategoriesNames.size(); x++) {
            if (loadedCategoriesNames.get(x).equals(title.toUpperCase())) {
                listPosition = x;

            }
        }

        if (listPosition == 0) {
            loadedCategoriesNames.add(title.toUpperCase());
            lists.add(new ArrayList<HomePageModel>());
            loadFragmentData(categoryRecyclerView, this, loadedCategoriesNames.size() - 1, title);
        } else {
            adapter = new HomePageAdapter(lists.get(listPosition));

        }

        categoryRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_icon, menu);
        searchItem = menu.findItem(R.id.category_search_icon);

        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint("Search for Product");


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                loadingDialog.show();
                MainActivity.list.clear();
                ids.clear();


                final String[] tags = query.toLowerCase().split(" ");
                for (final String tag : tags) {
                    tag.trim();
                    FirebaseFirestore.getInstance().collection("PRODUCTS").whereArrayContains("tags", tag).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                    WishlistModel model = new WishlistModel(documentSnapshot.getId(), documentSnapshot.get("product_image_1").toString()
                                            , documentSnapshot.get("product_title").toString()
                                            , (long) documentSnapshot.get("free_coupons")
                                            , documentSnapshot.get("average_rating").toString()
                                            , (long) documentSnapshot.get("total_ratings")
                                            , documentSnapshot.get("product_price").toString()
                                            , documentSnapshot.get("cutted_price").toString()
                                            , (boolean) documentSnapshot.get("COD")
                                            , true);

                                    model.setTags((ArrayList<String>) documentSnapshot.get("tags"));
                                    if (!ids.contains(model.getProductId())) {
                                        MainActivity.list.add(model);
                                        ids.add(model.getProductId());
                                    }
                                }

                                if (tag.equals(tags[tags.length - 1])) {
                                    if (MainActivity.list.size() == 0) {
                                        Intent searchIntent = new Intent(CategoryActivity.this, SearchActivity.class);
                                        searchIntent.putExtra("textview", "VISIBLE");
                                        startActivity(searchIntent);
                                        loadingDialog.dismiss();
                                    } else {
                                        Intent searchIntent = new Intent(CategoryActivity.this, SearchActivity.class);
                                        searchIntent.putExtra("textview", "GONE");
                                        searchIntent.putExtra("query", query);
                                        startActivity(searchIntent);
                                        loadingDialog.dismiss();

                                    }
                                }

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(CategoryActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.main_search_icon) {
//            Intent searchIntent=new Intent(this,SearchActivity.class);
//            startActivity(searchIntent);
//            return true;
//        } else
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
