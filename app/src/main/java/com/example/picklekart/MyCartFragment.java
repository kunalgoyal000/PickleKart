package com.example.picklekart;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.picklekart.DBqueries.cartItemModelList;
import static com.example.picklekart.DBqueries.wishlistModelList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyCartFragment extends Fragment {


    public MyCartFragment() {
        // Required empty public constructor
    }

    private RecyclerView cartItemRecyclerView;
    private Button continueBtn;
    public static CartAdapter cartAdapter;
    private Dialog loadingDialog;
    private TextView totalCartAmount;
    private TextView emptyCart;
    private LinearLayout totalAmountContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_cart, container, false);


        /////////// Loading Dialog


        totalCartAmount = view.findViewById(R.id.total_cart_amount);
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));

        loadingDialog.show();

        //////////// Loading Dialog

        cartItemRecyclerView = view.findViewById(R.id.cart_items_recyclerview);
        totalAmountContainer = view.findViewById(R.id.total_amount_container);
        emptyCart = view.findViewById(R.id.tv_empty_cart);

        continueBtn = view.findViewById(R.id.cart_continue_btn);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        cartItemRecyclerView.setLayoutManager(linearLayoutManager);

        cartAdapter = new CartAdapter(cartItemModelList, totalCartAmount, true);
        cartItemRecyclerView.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();
        cartItemRecyclerView.setVisibility(View.VISIBLE);


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeliveryActivity.cartItemModelList = new ArrayList<>();
                DeliveryActivity.fromCart = true;
                for (int x = 0; x < cartItemModelList.size(); x++) {
                    CartItemModel cartItemModel = cartItemModelList.get(x);
                    if (cartItemModel.isInStock()) {
                        DeliveryActivity.cartItemModelList.add(cartItemModel);
                    }
                }
                DeliveryActivity.cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));

                loadingDialog.show();
                if (DBqueries.addressesModelList.size() == 0) {
                    DBqueries.loadAddresses(getContext(), loadingDialog, true);
                } else {
                    loadingDialog.dismiss();
                    Intent deliveryIntent = new Intent(getContext(), DeliveryActivity.class);
                    startActivity(deliveryIntent);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        cartAdapter.notifyDataSetChanged();
        if (DBqueries.rewardModelList.size() == 0) {
            loadingDialog.show();
            DBqueries.loadRewards(getContext(), loadingDialog, false);
        }
        if (cartItemModelList.size() == 0) {
            DBqueries.cartList.clear();
            DBqueries.loadCartList(getContext(), loadingDialog, true, new TextView(getContext()), totalCartAmount);
        } else {
            if (cartItemModelList.get(cartItemModelList.size() - 1).getType() == CartItemModel.TOTAL_AMOUNT) {
                LinearLayout parent = (LinearLayout) totalCartAmount.getParent().getParent();
                parent.setVisibility(View.VISIBLE);
            }
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (CartItemModel cartItemModel : cartItemModelList) {
            if (!TextUtils.isEmpty(cartItemModel.getSelectedCouponId())) {
                for (RewardModel rewardModel : DBqueries.rewardModelList) {
                    if (rewardModel.getCouponId().equals(cartItemModel.getSelectedCouponId())) {
                        rewardModel.setAlreadyUsed(false);
                    }
                }
                cartItemModel.setSelectedCouponId(null);
                if (MyRewardsFragment.rewardsAdapter != null) {
                    MyRewardsFragment.rewardsAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
