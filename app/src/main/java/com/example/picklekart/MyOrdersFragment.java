package com.example.picklekart;


import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static com.example.picklekart.DBqueries.myOrderItemModelList;
import static com.example.picklekart.DBqueries.wishlistModelList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyOrdersFragment extends Fragment {


    private Dialog loadingDialog;
    private TextView noOrders;

    public MyOrdersFragment() {
        // Required empty public constructor
    }

    private RecyclerView myOrdersRecyclerView;
    public static MyOrderAdapter myOrderAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_orders, container, false);

        /////////// Loading Dialog

        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));
        loadingDialog.show();
        //////////// Loading Dialog


        myOrdersRecyclerView = view.findViewById(R.id.my_orders_recyclerview);
        noOrders = view.findViewById(R.id.tv_no_orders);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        myOrdersRecyclerView.setLayoutManager(linearLayoutManager);
        myOrderAdapter = new MyOrderAdapter(DBqueries.myOrderItemModelList, loadingDialog);
        myOrdersRecyclerView.setAdapter(myOrderAdapter);
        myOrdersRecyclerView.setVisibility(View.VISIBLE);


        if (myOrderItemModelList.size() == 0) {
            myOrderItemModelList.clear();
//            myOrdersRecyclerView.setVisibility(View.GONE);
//            noOrders.setVisibility(View.VISIBLE);
            DBqueries.loadOrders(getContext(), myOrderAdapter, loadingDialog);

        } else {
            loadingDialog.dismiss();
//            myOrdersRecyclerView.setVisibility(View.VISIBLE);
//            noOrders.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        myOrderAdapter.notifyDataSetChanged();
    }
}
