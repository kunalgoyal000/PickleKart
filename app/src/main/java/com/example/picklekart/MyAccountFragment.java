package com.example.picklekart;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountFragment extends Fragment {


    private Dialog loadingDialog;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    private Button viewAllAddressBtn;
    public static final int MANAGE_ADDRESS = 1;
    private CircleImageView profileView, currentOrderImage;
    private TextView userName, userEmail, tvCurrentOrderStatus;
    private LinearLayout layoutContainer, recentOrdersContainer;
    private ImageView orderedIndicator, packedIndicator, shippedIndicator, deliveredIndicator;
    private ProgressBar O_P_progress, P_S_progress, S_D_progress;
    private TextView yourRecentOrdersTitle;
    private TextView addressname, address, pincode;
    private Button signOutBtn;

    private FloatingActionButton settingsBtn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);


        /////////// Loading Dialog

        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));
        loadingDialog.show();
        //////////// Loading Dialog


        viewAllAddressBtn = view.findViewById(R.id.view_all_addresses_button);
        profileView = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);
        layoutContainer = view.findViewById(R.id.layout_container);
        currentOrderImage = view.findViewById(R.id.current_order_image);
        tvCurrentOrderStatus = view.findViewById(R.id.tv_current_order_status);
        orderedIndicator = view.findViewById(R.id.ordered_indicator);
        packedIndicator = view.findViewById(R.id.packed_indicator);
        shippedIndicator = view.findViewById(R.id.shipped_indicator);
        deliveredIndicator = view.findViewById(R.id.delivered_indicator);
        O_P_progress = view.findViewById(R.id.ordered_packed_progress);
        P_S_progress = view.findViewById(R.id.packed_shipped_progress);
        S_D_progress = view.findViewById(R.id.shipped_delivered_progress);
        yourRecentOrdersTitle = view.findViewById(R.id.your_recent_orders_title);
        recentOrdersContainer = view.findViewById(R.id.recent_orders_container);
        addressname = view.findViewById(R.id.name);
        address = view.findViewById(R.id.address);
        pincode = view.findViewById(R.id.address_pincode);
        signOutBtn = view.findViewById(R.id.sign_out_btn);
        settingsBtn = view.findViewById(R.id.settings_btn);

        layoutContainer.getChildAt(1).setVisibility(View.GONE);
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                for (MyOrderItemModel myOrderItemModel : DBqueries.myOrderItemModelList) {

                    if (!myOrderItemModel.isCancellationRequested()) {
                        if (!myOrderItemModel.getOrderStatus().equals("Delivered") && !myOrderItemModel.getOrderStatus().equals("Cancelled")) {
                            layoutContainer.getChildAt(1).setVisibility(View.VISIBLE);
                            if (getContext() != null) {
                                Glide.with(getContext()).load(myOrderItemModel.getProductImage()).apply(new RequestOptions().placeholder(R.drawable.placeholder_icon)).into(currentOrderImage);
                            }
                            tvCurrentOrderStatus.setText(myOrderItemModel.getOrderStatus());

                            switch (myOrderItemModel.getOrderStatus()) {
                                case "Ordered":
                                    if (getContext() != null) {
                                        orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    }
                                    break;
                                case "Packed":
                                    if (getContext() != null) {
                                        orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                        packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    }
                                    O_P_progress.setProgress(100);
                                    break;
                                case "Shipped":
                                    if (getContext() != null) {
                                        orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                        packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                        shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    }
                                    O_P_progress.setProgress(100);
                                    P_S_progress.setProgress(100);

                                    break;
                                case "Out for Delivery":
                                    if (getContext() != null) {
                                        orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                        packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                        shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                        deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    }
                                    O_P_progress.setProgress(100);
                                    P_S_progress.setProgress(100);
                                    S_D_progress.setProgress(100);
                                    break;
                            }
                        }
                    }

                }
                int i = 0;
                for (MyOrderItemModel orderItemModel : DBqueries.myOrderItemModelList) {
                    if (i < 4) {
                        if (orderItemModel.getOrderStatus().equals("Delivered")) {
                            if (getContext() != null) {
                                Glide.with(getContext()).load(orderItemModel.getProductImage()).apply(new RequestOptions().placeholder(R.drawable.placeholder_icon)).into((CircleImageView) recentOrdersContainer.getChildAt(i));
                            }
                            i++;
                        }
                    } else {
                        break;
                    }
                }
                if (i == 0) {
                    yourRecentOrdersTitle.setText("No Recent Orders");
                }
                if (i < 3) {
                    for (int x = i; x < 4; x++) {
                        recentOrdersContainer.getChildAt(x).setVisibility(View.GONE);
                    }
                }
                loadingDialog.show();
                loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        loadingDialog.setOnDismissListener(null);
                        if (getContext() != null) {
                            if (DBqueries.addressesModelList.size() == 0) {
                                addressname.setText("No Address");
                                address.setText("-");
                                pincode.setText("-");
                            } else {
                                setAddress();
                            }
                        }
                    }
                });
                DBqueries.loadAddresses(getContext(), loadingDialog, false);
            }
        });
        DBqueries.loadOrders(getContext(), null, loadingDialog);

        viewAllAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myAddressesIntent = new Intent(getContext(), MyAddressesActivity.class);
                myAddressesIntent.putExtra("MODE", MANAGE_ADDRESS);
                startActivity(myAddressesIntent);
            }
        });


        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                DBqueries.clearData();
                Intent registerIntent = new Intent(getContext(), RegisterActivity.class);
                startActivity(registerIntent);
                getActivity().finish();
            }
        });


        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent updateUserInfo = new Intent(getContext(), UpdateUserInfoActivity.class);
                updateUserInfo.putExtra("Name", userName.getText());
                updateUserInfo.putExtra("Email", userEmail.getText());
                updateUserInfo.putExtra("Photo", DBqueries.profile);

                startActivity(updateUserInfo);
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null) {

            userName.setText(DBqueries.fullName);
            userEmail.setText(DBqueries.email);
        }
        if (!DBqueries.profile.equals("")) {
            if (getContext() != null) {
                Glide.with(getContext()).load(DBqueries.profile).apply(new RequestOptions().placeholder(R.drawable.placeholder_image_icon)).into(profileView);
            }
        } else {
            if (getContext() != null) {
                profileView.setImageResource(R.drawable.placeholder_image_icon);
            }
        }


        if (!loadingDialog.isShowing()) {
            if (getContext() != null) {
                if (DBqueries.addressesModelList.size() == 0) {
                    addressname.setText("No Address");
                    address.setText("-");
                    pincode.setText("-");
                } else {
                    setAddress();
                }
            }
        }
    }

    private void setAddress() {

        String nameText, mobileNo;
        nameText = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getName();
        mobileNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getMobileNo();
        if (DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo().equals("")) {

            addressname.setText(nameText + " - " + mobileNo);
        } else {
            addressname.setText(nameText + " - " + mobileNo + " or " + DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo());

        }
        String flatNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getFlatNo();
        String locality = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLocality();
        String landmark = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLandmark();
        String city = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getCity();
        String state = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getState();
        if (landmark.equals("")) {
            address.setText(flatNo + "," + locality + "," + city + "," + state);

        } else {
            address.setText(flatNo + "," + locality + "," + landmark + "," + city + "," + state);
        }
        pincode.setText(DBqueries.addressesModelList.get(DBqueries.selectedAddress).getPincode());
    }


}
