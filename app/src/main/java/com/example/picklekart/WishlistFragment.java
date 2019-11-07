package com.example.picklekart;


import android.app.Dialog;
import android.net.nsd.NsdManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.picklekart.DBqueries.wishlistModelList;


/**
 * A simple {@link Fragment} subclass.
 */
public class WishlistFragment extends Fragment {


    public WishlistFragment() {
        // Required empty public constructor
    }

    private RecyclerView wishlistRecyclerView;
    private TextView emptyWishlist;
    private Dialog loadingDialog;
    public static WishlistAdapter wishlistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);


        /////////// Loading Dialog


        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));

        loadingDialog.show();

        //////////// Loading Dialog
        wishlistRecyclerView = view.findViewById(R.id.my_wishlist_recyclerview);
        emptyWishlist = view.findViewById(R.id.tv_empty_wishlist);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        wishlistRecyclerView.setLayoutManager(linearLayoutManager);
        wishlistRecyclerView.setVisibility(View.VISIBLE);

        if (wishlistModelList.size() == 0) {
            DBqueries.wishlist.clear();
            DBqueries.loadWishlist(getContext(), loadingDialog, true);
        } else {
            loadingDialog.dismiss();
        }


        wishlistAdapter = new WishlistAdapter(DBqueries.wishlistModelList, true);
        wishlistRecyclerView.setAdapter(wishlistAdapter);
        wishlistAdapter.notifyDataSetChanged();

        return view;
    }


}
