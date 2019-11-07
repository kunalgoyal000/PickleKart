package com.example.picklekart;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.picklekart.DBqueries.categoryModelList;
import static com.example.picklekart.DBqueries.firebaseFirestore;
import static com.example.picklekart.DBqueries.lists;
import static com.example.picklekart.DBqueries.loadCategories;
import static com.example.picklekart.DBqueries.loadFragmentData;
import static com.example.picklekart.DBqueries.loadedCategoriesNames;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty public constructor
    }

    public static SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView categoryRecyclerView;
    private List<CategoryModel> categoryModelFakeList = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private RecyclerView homePageRecyclerView;
    private List<HomePageModel> homePageModelFakeList = new ArrayList<>();
    private HomePageAdapter adapter;
    private ImageView noInternetConnection;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private Button retryButton;

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        swipeRefreshLayout = view.findViewById(R.id.refresh_layout);
        noInternetConnection = view.findViewById(R.id.no_internet_connection);
        categoryRecyclerView = view.findViewById(R.id.category_recyclerview);
        homePageRecyclerView = view.findViewById(R.id.home_page_recyclerview);
        retryButton = view.findViewById(R.id.retry_button);

        swipeRefreshLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.colorPrimary), getContext().getResources().getColor(R.color.colorPrimary), getContext().getResources().getColor(R.color.colorPrimary));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        categoryRecyclerView.setLayoutManager(layoutManager);


        LinearLayoutManager testingLayoutManager = new LinearLayoutManager(getContext());
        testingLayoutManager.setOrientation(RecyclerView.VERTICAL);
        homePageRecyclerView.setLayoutManager(testingLayoutManager);


        //////////////////// Categories fake list
        categoryModelFakeList.add(new CategoryModel("null", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));
        categoryModelFakeList.add(new CategoryModel("", ""));


        //////////////////// Categories fake list


        //////////////////// Home Page fake list

        List<SliderModel> sliderModelFakeList = new ArrayList<>();
        sliderModelFakeList.add(new SliderModel("null", "#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null", "#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null", "#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null", "#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null", "#dfdfdf"));

        List<HorizontalProductScrollModel> horizontalProductScrollModelFakeList = new ArrayList<>();
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("", "", "", "", ""));

        homePageModelFakeList.add(new HomePageModel(0, sliderModelFakeList));
        homePageModelFakeList.add(new HomePageModel(1, "", "#dfdfdf"));
        homePageModelFakeList.add(new HomePageModel(2, "", "#dfdfdf", horizontalProductScrollModelFakeList, new ArrayList<WishlistModel>()));
        homePageModelFakeList.add(new HomePageModel(3, "", "#dfdfdf", horizontalProductScrollModelFakeList));

        //////////////////// Home Page fake list


        categoryAdapter = new CategoryAdapter(categoryModelFakeList);

        adapter = new HomePageAdapter(homePageModelFakeList);

        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() == true) {
            MainActivity.drawer.setDrawerLockMode(0);
            noInternetConnection.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
            homePageRecyclerView.setVisibility(View.VISIBLE);


            if (categoryModelList.size() == 0) {
                loadCategories(categoryRecyclerView, getContext());
            } else {
                categoryAdapter = new CategoryAdapter(categoryModelList);
                categoryAdapter.notifyDataSetChanged();
            }
            categoryRecyclerView.setAdapter(categoryAdapter);


            if (lists.size() == 0) {
                loadedCategoriesNames.add("HOME");
                lists.add(new ArrayList<HomePageModel>());
                loadFragmentData(homePageRecyclerView, getContext(), 0, "Home");
            } else {
                adapter = new HomePageAdapter(lists.get(0));
                adapter.notifyDataSetChanged();
            }
            homePageRecyclerView.setAdapter(adapter);

        } else {
            MainActivity.drawer.setDrawerLockMode(1);
            categoryRecyclerView.setVisibility(View.GONE);
            homePageRecyclerView.setVisibility(View.GONE);
            Glide.with(this).load(R.drawable.no_internet_connection).into(noInternetConnection);
            noInternetConnection.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);

        }


        //// refresh layout

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                reloadPage();
            }
        });

        //// refresh layout


        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadPage();
            }
        });
        return view;
    }

    @SuppressLint("WrongConstant")
    private void reloadPage() {
        networkInfo = connectivityManager.getActiveNetworkInfo();
        DBqueries.clearData();
        networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected() == true) {
            MainActivity.drawer.setDrawerLockMode(0);
            noInternetConnection.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Internet is Available!!", Toast.LENGTH_SHORT).show();
            categoryRecyclerView.setVisibility(View.VISIBLE);
            homePageRecyclerView.setVisibility(View.VISIBLE);

            categoryAdapter = new CategoryAdapter(categoryModelFakeList);
            adapter = new HomePageAdapter(homePageModelFakeList);
            categoryRecyclerView.setAdapter(categoryAdapter);
            homePageRecyclerView.setAdapter(adapter);
            loadCategories(categoryRecyclerView, getContext());


            loadedCategoriesNames.add("HOME");
            lists.add(new ArrayList<HomePageModel>());
            loadFragmentData(homePageRecyclerView, getContext(), 0, "Home");
        } else {
            MainActivity.drawer.setDrawerLockMode(1);
            Toast.makeText(getContext(), "No Internet Connection!!", Toast.LENGTH_SHORT).show();
            categoryRecyclerView.setVisibility(View.GONE);
            homePageRecyclerView.setVisibility(View.GONE);
            Glide.with(getContext()).load(R.drawable.no_internet_connection).into(noInternetConnection);
            noInternetConnection.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);

            swipeRefreshLayout.setRefreshing(false);
        }
    }

}

