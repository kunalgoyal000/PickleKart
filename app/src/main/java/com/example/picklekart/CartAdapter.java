package com.example.picklekart;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.picklekart.DBqueries.firebaseFirestore;
import static com.example.picklekart.MyCartFragment.cartAdapter;

public class CartAdapter extends RecyclerView.Adapter {

    List<CartItemModel> cartItemModelList;
    private int lastPosition = -1;
    private TextView totalCartAmount;
    private boolean showDeleteBtn;

    ///////////coupon dialog

    private TextView couponTitle;
    private TextView couponExpiryDate;
    private TextView couponBody;
    private RecyclerView couponRecyclerView;
    private LinearLayout selectedCoupon;
    private TextView discountedPrice;
    private TextView originalPrice;
    private Button removeCouponBtn, applyCouponBtn;
    private LinearLayout applyOrRemoveBtnContainer;
    private TextView footerText;
    private String productOriginalPrice;
    ///////////coupon dialog

    public CartAdapter(List<CartItemModel> cartItemModelList, TextView totalCartAmount, boolean showDeleteBtn) {
        this.cartItemModelList = cartItemModelList;
        this.totalCartAmount = totalCartAmount;
        this.showDeleteBtn = showDeleteBtn;
    }

    @Override
    public int getItemViewType(int position) {

        switch (cartItemModelList.get(position).getType()) {
            case 0:
                return CartItemModel.CART_ITEM;
            case 1:
                return CartItemModel.TOTAL_AMOUNT;
            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case CartItemModel.CART_ITEM:
                View cartItemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_layout, parent, false);
                return new CartItemViewHolder(cartItemview);
            case CartItemModel.TOTAL_AMOUNT:
                View cartTotalview = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_total_amount_layout, parent, false);
                return new CartTotalAmountViewHolder(cartTotalview);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (cartItemModelList.get(position).getType()) {
            case CartItemModel.CART_ITEM:
                String productID = cartItemModelList.get(position).getProductID();
                String resource = cartItemModelList.get(position).getProductImage();
                String title = cartItemModelList.get(position).getProductTitle();
                Long freeCoupons = cartItemModelList.get(position).getFreeCoupons();
                String productPrice = cartItemModelList.get(position).getProductPrice();
                String cuttedPrice = cartItemModelList.get(position).getCuttedPrice();
                Long offersApplied = cartItemModelList.get(position).getOffersApplied();
                boolean inStock = cartItemModelList.get(position).isInStock();
                Long productQuantity = cartItemModelList.get(position).getProductQuantity();
                Long maxQuantity = cartItemModelList.get(position).getMaxQuantity();
                boolean qtyError = cartItemModelList.get(position).isQtyError();
                List<String> qtyIds = cartItemModelList.get(position).getQtyIDs();
                long stockQty = cartItemModelList.get(position).getStockQuantity();
                boolean COD = cartItemModelList.get(position).isCOD();

                ((CartItemViewHolder) holder).setItemDetails(productID, resource, title, freeCoupons, productPrice, cuttedPrice, offersApplied, position, inStock, String.valueOf(productQuantity), maxQuantity, qtyError, qtyIds, stockQty, COD);

                break;
            case CartItemModel.TOTAL_AMOUNT:

                int totalItems = 0;
                int totalItemPrice = 0;
                String deliveryPrice;
                int totalAmount;
                int savedAmount = 0;

                for (int x = 0; x < cartItemModelList.size(); x++) {
                    if (cartItemModelList.get(x).getType() == CartItemModel.CART_ITEM && cartItemModelList.get(x).isInStock()) {
                        int quantity = Integer.parseInt(String.valueOf(cartItemModelList.get(x).getProductQuantity()));
                        totalItems = totalItems + quantity;
                        if (TextUtils.isEmpty(cartItemModelList.get(x).getSelectedCouponId())) {
                            totalItemPrice = totalItemPrice + Integer.parseInt(cartItemModelList.get(x).getProductPrice()) * quantity;
                        } else {
                            totalItemPrice = totalItemPrice + Integer.parseInt(cartItemModelList.get(x).getDiscountedPrice()) * quantity;

                        }

                        if (!TextUtils.isEmpty(cartItemModelList.get(x).getCuttedPrice())) {
                            savedAmount = savedAmount + (Integer.parseInt(cartItemModelList.get(x).getCuttedPrice()) - Integer.parseInt(cartItemModelList.get(x).getProductPrice())) * quantity;
                            if (!TextUtils.isEmpty(cartItemModelList.get(x).getSelectedCouponId())) {
                                savedAmount = savedAmount + (Integer.parseInt(cartItemModelList.get(x).getProductPrice()) - Integer.parseInt(cartItemModelList.get(x).getDiscountedPrice())) * quantity;
                            }
                        } else {
                            if (!TextUtils.isEmpty(cartItemModelList.get(x).getSelectedCouponId())) {
                                savedAmount = savedAmount + (Integer.parseInt(cartItemModelList.get(x).getProductPrice()) - Integer.parseInt(cartItemModelList.get(x).getDiscountedPrice())) * quantity;
                            }

                        }
                    }
                    if (totalItemPrice > 500) {
                        deliveryPrice = "FREE";
                        totalAmount = totalItemPrice;
                    } else {
                        deliveryPrice = "60";
                        totalAmount = totalItemPrice + 60;
                    }
                    cartItemModelList.get(position).setTotalItems(totalItems);
                    cartItemModelList.get(position).setTotalItemPrice(totalItemPrice);
                    cartItemModelList.get(position).setTotalAmount(totalAmount);
                    cartItemModelList.get(position).setSavedAmount(savedAmount);
                    cartItemModelList.get(position).setDeliveryPrice(deliveryPrice);

                    ((CartTotalAmountViewHolder) holder).setTotalAmount(totalItems, totalItemPrice, deliveryPrice, totalAmount, savedAmount);
                    break;

                }
            default:
                return;
        }

        if (lastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
            holder.itemView.setAnimation(animation);
            lastPosition = position;
        }

    }

    @Override
    public int getItemCount() {
        return cartItemModelList.size();
    }


    class CartItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private ImageView freeCouponIcon;
        private TextView productTitle;
        private TextView freeCoupons;
        private TextView productPrice;
        private TextView cuttedPrice;
        private TextView productQuantity;
        private TextView offersApplied;
        private TextView couponsApplied;
        private LinearLayout deleteBtn;
        private Button redeemBtn;
        private LinearLayout couponRedemptionLayout;
        private TextView couponRedemptionBody;

        private ImageView codIndicator;


        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productTitle = itemView.findViewById(R.id.product_title);
            productPrice = itemView.findViewById(R.id.product_price);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            cuttedPrice = itemView.findViewById(R.id.cutted_price);
            offersApplied = itemView.findViewById(R.id.offers_applied);
            couponsApplied = itemView.findViewById(R.id.coupons_applied);
            freeCoupons = itemView.findViewById(R.id.tv_free_coupon);
            freeCouponIcon = itemView.findViewById(R.id.free_coupon_icon);
            deleteBtn = itemView.findViewById(R.id.remove_item_btn);
            redeemBtn = itemView.findViewById(R.id.coupon_redemption_btn);
            couponRedemptionLayout = itemView.findViewById(R.id.coupon_redemption_layout);
            couponRedemptionBody = itemView.findViewById(R.id.tv_coupon_redemption);
            codIndicator = itemView.findViewById(R.id.cod_indicator);

        }

        private void setItemDetails(final String productId, String resource, String title, Long freeCouponsNum, final String productPriceText, String cuttedPriceText, Long offersAppliedNum, final int position, final boolean inStock, final String quantity, final Long maxQuantity, boolean qtyError, final List<String> qtyIds, final long stockQuantity, boolean COD) {
            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions()).placeholder(R.drawable.placeholder_icon).into(productImage);
            productTitle.setText(title);
            productPrice.setText(productPriceText);
            cuttedPrice.setText(cuttedPriceText);
            final Dialog checkCouponPriceDialog = new Dialog(itemView.getContext());
            checkCouponPriceDialog.setContentView(R.layout.coupon_redeem_dialog);
            checkCouponPriceDialog.setCancelable(false);
            checkCouponPriceDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (COD) {
                codIndicator.setVisibility(View.VISIBLE);
            } else {
                codIndicator.setVisibility(View.INVISIBLE);
            }

            if (inStock) {
                productPrice.setText("Rs." + productPriceText + "/-");
                productPrice.setTextColor(Color.parseColor("#000000"));
                cuttedPrice.setText("Rs." + cuttedPriceText + "/-");
                couponRedemptionLayout.setVisibility(View.VISIBLE);
                ///////////// COUPON DIALOG


                ImageView togglebtn = checkCouponPriceDialog.findViewById(R.id.toggle_recyclerview);
                couponRecyclerView = checkCouponPriceDialog.findViewById(R.id.coupons_recyclerview);
                selectedCoupon = checkCouponPriceDialog.findViewById(R.id.selected_coupon);

                couponTitle = checkCouponPriceDialog.findViewById(R.id.coupon_title);
                couponExpiryDate = checkCouponPriceDialog.findViewById(R.id.coupon_validity);
                couponBody = checkCouponPriceDialog.findViewById(R.id.coupon_body);
                removeCouponBtn = checkCouponPriceDialog.findViewById(R.id.remove_btn);
                applyCouponBtn = checkCouponPriceDialog.findViewById(R.id.apply_btn);
                applyOrRemoveBtnContainer = checkCouponPriceDialog.findViewById(R.id.apply_or_remove_btns_container);
                footerText = checkCouponPriceDialog.findViewById(R.id.footer_text);

                footerText.setVisibility(View.GONE);
                applyOrRemoveBtnContainer.setVisibility(View.VISIBLE);

                originalPrice = checkCouponPriceDialog.findViewById(R.id.original_price);
                discountedPrice = checkCouponPriceDialog.findViewById(R.id.discounted_price);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext());
                linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
                couponRecyclerView.setLayoutManager(linearLayoutManager);

                originalPrice.setText(productPrice.getText());
                productOriginalPrice = productPriceText;
                RewardsAdapter rewardsAdapter = new RewardsAdapter(position, DBqueries.rewardModelList, true, couponRecyclerView, selectedCoupon, productOriginalPrice, couponTitle, couponExpiryDate, couponBody, discountedPrice, cartItemModelList);
                couponRecyclerView.setAdapter(rewardsAdapter);
                rewardsAdapter.notifyDataSetChanged();

                applyCouponBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(cartItemModelList.get(position).getSelectedCouponId())) {
                            for (RewardModel rewardModel : DBqueries.rewardModelList) {
                                if (rewardModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                                    rewardModel.setAlreadyUsed(true);
                                    couponRedemptionLayout.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.reward_gradient_background));
                                    couponRedemptionBody.setText(rewardModel.getCouponBody());
                                    redeemBtn.setText("Coupon");

                                }
                            }
                            couponsApplied.setVisibility(View.VISIBLE);
                            cartItemModelList.get(position).setDiscountedPrice(discountedPrice.getText().toString().substring(3, discountedPrice.getText().length() - 2));
                            productPrice.setText(discountedPrice.getText());
                            String offerDiscountedAmount = String.valueOf(Long.valueOf(productPriceText) - Long.valueOf(discountedPrice.getText().toString().substring(3, discountedPrice.getText().length() - 2)));
                            couponsApplied.setText("Coupon Applied - Rs." + offerDiscountedAmount + "/-");
                            notifyItemChanged(cartItemModelList.size() - 1);
                            checkCouponPriceDialog.dismiss();
                        }
                    }
                });

                removeCouponBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (RewardModel rewardModel : DBqueries.rewardModelList) {
                            if (rewardModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                                rewardModel.setAlreadyUsed(false);
                            }
                        }
                        couponTitle.setText("Coupon");
                        couponExpiryDate.setText("Validity");
                        couponBody.setText("Tap the icon on the top right corner to select your coupon.");
                        couponsApplied.setVisibility(View.INVISIBLE);
                        couponRedemptionLayout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.couponRed));
                        couponRedemptionBody.setText("Apply your coupon here!");
                        redeemBtn.setText("Redeem");
                        cartItemModelList.get(position).setSelectedCouponId(null);
                        productPrice.setText("Rs." + productPriceText + "/-");
                        notifyItemChanged(cartItemModelList.size() - 1);
                        checkCouponPriceDialog.dismiss();
                    }
                });

                togglebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialogRecyclerView();
                    }
                });


                if (!TextUtils.isEmpty(cartItemModelList.get(position).getSelectedCouponId())) {
                    for (RewardModel rewardModel : DBqueries.rewardModelList) {
                        if (rewardModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                            couponRedemptionLayout.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.reward_gradient_background));
                            couponRedemptionBody.setText(rewardModel.getCouponBody());
                            redeemBtn.setText("Coupon");

                            couponBody.setText(rewardModel.getCouponBody());
                            if (rewardModel.getType().equals("Discount")) {
                                couponTitle.setText(rewardModel.getType());
                            } else {
                                couponTitle.setText("FLAT Rs." + rewardModel.getDiscount() + " OFF");

                            }
                            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM YYYY");
                            couponExpiryDate.setText("Till " + simpleDateFormat.format(rewardModel.getTimestamp()));

                        }
                    }
                    discountedPrice.setText("Rs." + cartItemModelList.get(position).getDiscountedPrice() + "/-");
                    couponsApplied.setVisibility(View.VISIBLE);
                    productPrice.setText("Rs." + cartItemModelList.get(position).getDiscountedPrice() + "/-");
                    String offerDiscountedAmount = String.valueOf(Long.valueOf(productPriceText) - Long.valueOf(cartItemModelList.get(position).getDiscountedPrice()));
                    couponsApplied.setText("Coupon Applied - Rs." + offerDiscountedAmount + "/-");
                } else {
                    couponsApplied.setVisibility(View.INVISIBLE);
                    couponRedemptionLayout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.couponRed));
                    couponRedemptionBody.setText("Apply your coupon here!");
                    redeemBtn.setText("Redeem");
                }

                ///////////// COUPON DIALOG


                productQuantity.setText("Qty: " + quantity);
                if (!showDeleteBtn) {
                    if (qtyError) {
                        productQuantity.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                        productQuantity.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.colorPrimary)));
                    } else {
                        productQuantity.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
                        productQuantity.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(android.R.color.black)));
                    }
                }

                productQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog quantityDialog = new Dialog(itemView.getContext());
                        quantityDialog.setContentView(R.layout.quantity_dialog);
                        quantityDialog.setCancelable(false);
                        quantityDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        final EditText quantityNo = quantityDialog.findViewById(R.id.quantity_no);
                        Button dialogCancelBtn = quantityDialog.findViewById(R.id.cancel_btn);
                        Button dialogOkBtn = quantityDialog.findViewById(R.id.ok_btn);
                        quantityNo.setHint("Max " + String.valueOf(maxQuantity));

                        dialogCancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                quantityDialog.dismiss();

                            }
                        });
                        dialogOkBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!TextUtils.isEmpty(quantityNo.getText())) {
                                    if (Long.valueOf(quantityNo.getText().toString()) <= maxQuantity && Long.valueOf(quantityNo.getText().toString()) != 0) {
                                        if (itemView.getContext() instanceof MainActivity) {
                                            cartItemModelList.get(position).setProductQuantity(Long.valueOf(quantityNo.getText().toString()));

                                        } else {
                                            if (DeliveryActivity.fromCart) {
                                                cartItemModelList.get(position).setProductQuantity(Long.valueOf(quantityNo.getText().toString()));
                                            } else {
                                                DeliveryActivity.cartItemModelList.get(position).setProductQuantity(Long.valueOf(quantityNo.getText().toString()));
                                            }
                                        }
                                        productQuantity.setText("Qty: " + quantityNo.getText());
                                        notifyItemChanged(cartItemModelList.size() - 1);
                                        if (!showDeleteBtn) {
                                            DeliveryActivity.loadingDialog.show();
                                            DeliveryActivity.cartItemModelList.get(position).setQtyError(false);
                                            final int initialQty = Integer.parseInt(quantity);
                                            final int finalQty = Integer.parseInt(quantityNo.getText().toString());
                                            final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                                            if (finalQty > initialQty) {
                                                for (int y = 0; y < finalQty - initialQty; y++) {
                                                    final String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);
                                                    Map<String, Object> timeStamp = new HashMap<>();
                                                    timeStamp.put("time", FieldValue.serverTimestamp());
                                                    final int finalY = y;
                                                    firebaseFirestore.collection("PRODUCTS").document(productId).collection("QUANTITY").document(quantityDocumentName)
                                                            .set(timeStamp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            qtyIds.add(quantityDocumentName);

                                                            if (finalY + 1 == finalQty - initialQty) {

                                                                firebaseFirestore.collection("PRODUCTS").document(productId).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).limit(stockQuantity).get()
                                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    List<String> serverQuantity = new ArrayList<>();

                                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                                                        serverQuantity.add(queryDocumentSnapshot.getId());
                                                                                    }

                                                                                    long availableQty = 0;
                                                                                    for (String qtyId : qtyIds) {

                                                                                        if (!serverQuantity.contains(qtyId)) {

                                                                                            DeliveryActivity.cartItemModelList.get(position).setQtyError(true);
                                                                                            DeliveryActivity.cartItemModelList.get(position).setMaxQuantity(availableQty);
                                                                                            Toast.makeText(itemView.getContext(), "Sorry ! All products may not be available in required quantity....", Toast.LENGTH_SHORT).show();

                                                                                        } else {
                                                                                            availableQty++;
                                                                                        }
                                                                                    }
                                                                                    DeliveryActivity.cartAdapter.notifyDataSetChanged();
                                                                                } else {
                                                                                    String error = task.getException().getMessage();
                                                                                    Toast.makeText(itemView.getContext(), error, Toast.LENGTH_SHORT).show();
                                                                                    //////error
                                                                                }
                                                                                DeliveryActivity.loadingDialog.dismiss();
                                                                            }

                                                                        });
                                                            }
                                                        }

                                                    });
                                                }

                                            } else if (initialQty > finalQty) {
                                                for (int x = 0; x < initialQty - finalQty; x++) {
                                                    final String qtyId = qtyIds.get(qtyIds.size() - 1 - x);
                                                    final int finalX = x;
                                                    firebaseFirestore.collection("PRODUCTS").document(productId).collection("QUANTITY").document(qtyId).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    qtyIds.remove(qtyId);
                                                                    DeliveryActivity.cartAdapter.notifyDataSetChanged();
                                                                    if (finalX + 1 == initialQty - finalQty) {
                                                                        DeliveryActivity.loadingDialog.dismiss();

                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(itemView.getContext(), "Max Quantity : " + maxQuantity.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                quantityDialog.dismiss();

                            }
                        });
                        quantityDialog.show();
                    }
                });


                if (offersAppliedNum > 0) {
                    offersApplied.setVisibility(View.VISIBLE);
                    String offerDiscountedAmount = String.valueOf(Long.valueOf(cuttedPriceText) - Long.valueOf(productPriceText));
                    offersApplied.setText("Offer Applied - Rs." + offerDiscountedAmount + "/-");
                } else {
                    offersApplied.setVisibility(View.INVISIBLE);

                }

                if (freeCouponsNum > 0) {
                    freeCouponIcon.setVisibility(View.VISIBLE);
                    freeCoupons.setVisibility(View.VISIBLE);
                    if (freeCouponsNum == 1) {
                        freeCoupons.setText("Free " + freeCouponsNum + " Coupon");
                    } else {
                        freeCoupons.setText("Free " + freeCouponsNum + " Coupons");
                    }
                } else {
                    freeCouponIcon.setVisibility(View.INVISIBLE);
                    freeCoupons.setVisibility(View.INVISIBLE);
                }

            } else {
                productPrice.setText("Out of Stock");
                productPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                cuttedPrice.setText("");

                couponRedemptionLayout.setVisibility(View.GONE);
                productQuantity.setVisibility(View.INVISIBLE);
                freeCoupons.setVisibility(View.INVISIBLE);
                couponsApplied.setVisibility(View.GONE);
                offersApplied.setVisibility(View.GONE);
                freeCouponIcon.setVisibility(View.INVISIBLE);

            }


            if (showDeleteBtn) {
                deleteBtn.setVisibility(View.VISIBLE);
            } else {
                deleteBtn.setVisibility(View.GONE);
            }

            redeemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (RewardModel rewardModel : DBqueries.rewardModelList) {
                        if (rewardModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                            rewardModel.setAlreadyUsed(false);
                        }
                    }
                    checkCouponPriceDialog.show();
                }
            });
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(cartItemModelList.get(position).getSelectedCouponId())) {
                        for (RewardModel rewardModel : DBqueries.rewardModelList) {
                            if (rewardModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                                rewardModel.setAlreadyUsed(false);
                            }
                        }
                    }
                    if (!ProductDetailsActivity.RUNNING_CART_QUERY) {
                        ProductDetailsActivity.RUNNING_CART_QUERY = true;

                        DBqueries.removeFromCart(position, itemView.getContext(), totalCartAmount);
                    }
                }
            });
        }

        private void showDialogRecyclerView() {
            if (couponRecyclerView.getVisibility() == View.GONE) {
                couponRecyclerView.setVisibility(View.VISIBLE);
                selectedCoupon.setVisibility(View.GONE);

            } else {
                couponRecyclerView.setVisibility(View.GONE);
                selectedCoupon.setVisibility(View.VISIBLE);

            }
        }

    }


    class CartTotalAmountViewHolder extends RecyclerView.ViewHolder {

        private TextView totalItems;
        private TextView totalItemPrice;
        private TextView deliveryPrice;
        private TextView totalAmount;
        private TextView savedAmount;

        public CartTotalAmountViewHolder(@NonNull View itemView) {
            super(itemView);
            totalItems = itemView.findViewById(R.id.total_items);
            totalItemPrice = itemView.findViewById(R.id.total_items_price);
            deliveryPrice = itemView.findViewById(R.id.delivery_price);
            totalAmount = itemView.findViewById(R.id.total_amount);
            savedAmount = itemView.findViewById(R.id.saved_amount);

        }


        private void setTotalAmount(int totalItemText, int totalItemPriceText, String deliveryPriceText, int totalAmountText, int savedAmountText) {
            totalItems.setText("Price(" + totalItemText + " items)");
            totalItemPrice.setText("Rs." + totalItemPriceText + "/-");
            if (deliveryPriceText.equals("FREE")) {
                deliveryPrice.setText(deliveryPriceText);

            } else {
                deliveryPrice.setText("Rs." + deliveryPriceText + "/-");

            }
            totalAmount.setText("Rs." + totalAmountText + "/-");
            totalCartAmount.setText("Rs." + totalAmountText + "/-");
            savedAmount.setText("You saved Rs." + savedAmountText + "/- on this order.");


            LinearLayout parent = (LinearLayout) totalCartAmount.getParent().getParent();
            if (totalItemPriceText == 0) {
                if (DeliveryActivity.fromCart) {
                    cartItemModelList.remove(cartItemModelList.size() - 1);
                    DeliveryActivity.cartItemModelList.remove(DeliveryActivity.cartItemModelList.size() - 1);

                }
                if (showDeleteBtn) {
                    cartItemModelList.remove(cartItemModelList.size() - 1);
                }
                parent.setVisibility(View.GONE);
            } else {
                parent.setVisibility(View.VISIBLE);
            }
        }

    }
}




