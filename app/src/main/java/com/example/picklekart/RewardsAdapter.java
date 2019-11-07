package com.example.picklekart;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.Viewholder> {

    List<RewardModel> rewardModelList;
    private Boolean useMiniLayout;
    private RecyclerView couponRecyclerView;
    private LinearLayout selectedCoupon;
    private String productOriginalPrice;
    private TextView discountedPrice;
    private TextView selectedCouponTitle;
    private TextView selectedCouponExpiryDate;
    private TextView selectedCouponBody;
    private int cartItemPosition = -1;
    private List<CartItemModel> cartItemModelList;

    public RewardsAdapter(List<RewardModel> rewardModelList, Boolean useMiniLayout) {
        this.rewardModelList = rewardModelList;
        this.useMiniLayout = useMiniLayout;
    }

    public RewardsAdapter(List<RewardModel> rewardModelList, Boolean useMiniLayout, RecyclerView couponRecyclerView, LinearLayout selectedCoupon, String productOriginalPrice, TextView selectedCouponTitle, TextView selectedCouponExpiryDate, TextView selectedCouponBody, TextView discountedPrice) {
        this.rewardModelList = rewardModelList;
        this.useMiniLayout = useMiniLayout;
        this.couponRecyclerView = couponRecyclerView;
        this.selectedCoupon = selectedCoupon;
        this.productOriginalPrice = productOriginalPrice;
        this.selectedCouponTitle = selectedCouponTitle;
        this.selectedCouponExpiryDate = selectedCouponExpiryDate;
        this.selectedCouponBody = selectedCouponBody;
        this.discountedPrice = discountedPrice;
    }

    public RewardsAdapter(int cartItemPosition, List<RewardModel> rewardModelList, Boolean useMiniLayout, RecyclerView couponRecyclerView, LinearLayout selectedCoupon, String productOriginalPrice, TextView selectedCouponTitle, TextView selectedCouponExpiryDate, TextView selectedCouponBody, TextView discountedPrice, List<CartItemModel> cartItemModelList) {
        this.cartItemPosition = cartItemPosition;
        this.rewardModelList = rewardModelList;
        this.useMiniLayout = useMiniLayout;
        this.couponRecyclerView = couponRecyclerView;
        this.selectedCoupon = selectedCoupon;
        this.productOriginalPrice = productOriginalPrice;
        this.selectedCouponTitle = selectedCouponTitle;
        this.selectedCouponExpiryDate = selectedCouponExpiryDate;
        this.selectedCouponBody = selectedCouponBody;
        this.discountedPrice = discountedPrice;
        this.cartItemModelList = cartItemModelList;
    }

    @NonNull
    @Override
    public RewardsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (useMiniLayout) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mini_rewards_item_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rewards_item_layout, parent, false);

        }
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardsAdapter.Viewholder holder, int position) {
        String couponId = rewardModelList.get(position).getCouponId();
        String type = rewardModelList.get(position).getType();
        Date validity = rewardModelList.get(position).getTimestamp();
        String body = rewardModelList.get(position).getCouponBody();
        String lowerLimit = rewardModelList.get(position).getLowerLimit();
        String upperLimit = rewardModelList.get(position).getUpperLimit();
        String discount = rewardModelList.get(position).getDiscount();
        Boolean alreadyUsed = rewardModelList.get(position).getAlreadyUsed();

        holder.setData(couponId, type, validity, body, lowerLimit, upperLimit, discount, alreadyUsed);
    }

    @Override
    public int getItemCount() {
        return rewardModelList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView couponTitle;
        private TextView couponExpiryDate;
        private TextView couponBody;

        public Viewholder(View itemView) {
            super(itemView);
            couponTitle = itemView.findViewById(R.id.coupon_title);
            couponExpiryDate = itemView.findViewById(R.id.coupon_validity);
            couponBody = itemView.findViewById(R.id.coupon_body);
        }

        private void setData(final String couponId, final String type, final Date validity, final String body, final String lowerLimit, final String upperLimit, final String discount, final Boolean alreadyUsed) {

            if (type.equals("Discount")) {
                couponTitle.setText(type);
            } else {
                couponTitle.setText("FLAT Rs." + discount + " OFF");

            }

            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM YYYY");

            if (alreadyUsed) {
                couponExpiryDate.setText("Already used");
                couponExpiryDate.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                couponBody.setTextColor(Color.parseColor("#50ffffff"));
                couponTitle.setTextColor(Color.parseColor("#50ffffff"));

            } else {
                couponExpiryDate.setText("Till " + simpleDateFormat.format(validity));
                couponBody.setTextColor(Color.parseColor("#ffffff"));
                couponTitle.setTextColor(Color.parseColor("#ffffff"));
                couponExpiryDate.setTextColor(itemView.getContext().getResources().getColor(R.color.couponPurple));
            }
            couponBody.setText(body);


            if (useMiniLayout) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!alreadyUsed) {
                            if (type.equals("Discount")) {
                                selectedCouponTitle.setText(type);
                            } else {
                                selectedCouponTitle.setText("FLAT Rs." + discount + " OFF");

                            }
                            selectedCouponExpiryDate.setText("Till " + simpleDateFormat.format(validity));
                            selectedCouponBody.setText(body);

                            if (Long.valueOf(productOriginalPrice) > Long.valueOf(lowerLimit) && Long.valueOf(productOriginalPrice) < Long.valueOf(upperLimit)) {
                                if (type.equals("Discount")) {
                                    Long discountAmount = Long.valueOf(productOriginalPrice) * Long.valueOf(discount) / 100;
                                    discountedPrice.setText("Rs." + String.valueOf(Long.valueOf(productOriginalPrice) - discountAmount) + "/-");
                                } else {
                                    discountedPrice.setText("Rs." + String.valueOf(Long.valueOf(productOriginalPrice) - Long.valueOf(discount)) + "/-");

                                }
                                if (cartItemPosition != -1) {
                                    cartItemModelList.get(cartItemPosition).setSelectedCouponId(couponId);
                                }
                            } else {
                                if (cartItemPosition != -1) {
                                    cartItemModelList.get(cartItemPosition).setSelectedCouponId(null);
                                }
                                discountedPrice.setText("Invalid");
                                Toast.makeText(itemView.getContext(), "Sorry!! Product does not matches the coupon terms.", Toast.LENGTH_SHORT).show();
                            }

                            if (couponRecyclerView.getVisibility() == View.GONE) {
                                couponRecyclerView.setVisibility(View.VISIBLE);
                                selectedCoupon.setVisibility(View.GONE);

                            } else {
                                couponRecyclerView.setVisibility(View.GONE);
                                selectedCoupon.setVisibility(View.VISIBLE);

                            }
                        }
                    }
                });
            }
        }
    }
}
