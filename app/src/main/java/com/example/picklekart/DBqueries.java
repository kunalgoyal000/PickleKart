package com.example.picklekart;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.example.picklekart.ProductDetailsActivity.ALREADY_ADDED_TO_CART;
import static com.example.picklekart.ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST;
import static com.example.picklekart.ProductDetailsActivity.RUNNING_CART_QUERY;
import static com.example.picklekart.ProductDetailsActivity.RUNNING_RATING_QUERY;
import static com.example.picklekart.ProductDetailsActivity.RUNNING_WISHLIST_QUERY;
import static com.example.picklekart.ProductDetailsActivity.addToWishlistBtn;
import static com.example.picklekart.ProductDetailsActivity.initialRating;
import static com.example.picklekart.ProductDetailsActivity.productID;
import static com.example.picklekart.ProductDetailsActivity.rateNowContainer;
import static com.example.picklekart.ProductDetailsActivity.setRating;

public class DBqueries {


    public static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public static String fullName, email, profile;
    public static List<CategoryModel> categoryModelList = new ArrayList<>();

    public static List<List<HomePageModel>> lists = new ArrayList<>();
    public static List<String> loadedCategoriesNames = new ArrayList<>();
    public static List<String> wishlist = new ArrayList<>();
    public static List<WishlistModel> wishlistModelList = new ArrayList<>();
    public static List<String> myRatedIds = new ArrayList<>();
    public static List<Long> myRating = new ArrayList<>();
    public static List<String> cartList = new ArrayList<>();
    public static List<CartItemModel> cartItemModelList = new ArrayList<>();

    public static int selectedAddress = -1;
    public static List<AddressesModel> addressesModelList = new ArrayList<>();
    public static List<RewardModel> rewardModelList = new ArrayList<>();
    public static List<MyOrderItemModel> myOrderItemModelList = new ArrayList<>();
    public static List<NotificationModel> notificationModelList = new ArrayList<>();
    public static ListenerRegistration registration;


    public static void loadCategories(final RecyclerView categoryRecyclerView, final Context context) {
        categoryModelList.clear();
        firebaseFirestore.collection("CATEGORIES").orderBy("index").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                categoryModelList.add(new CategoryModel(documentSnapshot.get("icon").toString(), documentSnapshot.get("categoryName").toString()));
                            }
                            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryModelList);
                            categoryRecyclerView.setAdapter(categoryAdapter);
                            categoryAdapter.notifyDataSetChanged();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void loadFragmentData(final RecyclerView homePageRecyclerView, final Context context, final int index, String categoryName) {
        firebaseFirestore.collection("CATEGORIES")
                .document(categoryName.toUpperCase())
                .collection("TOP_DEALS")
                .orderBy("index").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if ((long) documentSnapshot.get("view_type") == 0) {
                                    List<SliderModel> sliderModelList = new ArrayList<>();
                                    long no_of_banners = (long) documentSnapshot.get("no_of_banners");
                                    for (long x = 1; x < no_of_banners + 1; x++) {
                                        sliderModelList.add(new SliderModel(documentSnapshot.get("banner_" + x).toString()
                                                , documentSnapshot.get("banner_" + x + "_background").toString()));
                                    }
                                    lists.get(index).add(new HomePageModel(0, sliderModelList));
                                } else if ((long) documentSnapshot.get("view_type") == 1) {
                                    lists.get(index).add(new HomePageModel(1, documentSnapshot.get("strip_ad_banner").toString(),
                                            documentSnapshot.get("background").toString()));
                                } else if ((long) documentSnapshot.get("view_type") == 2) {

                                    List<WishlistModel> viewAllProductList = new ArrayList<>();
                                    List<HorizontalProductScrollModel> horizontalProductScrollModelList = new ArrayList<>();

                                    ArrayList<String> productIds = (ArrayList<String>) documentSnapshot.get("products");
                                    for (String productId : productIds) {
                                        horizontalProductScrollModelList.add(new HorizontalProductScrollModel(productId
                                                , ""
                                                , ""
                                                , ""
                                                , ""));

                                        viewAllProductList.add(new WishlistModel(productId
                                                , ""
                                                , ""
                                                , 0
                                                ,""
                                                , 0
                                                , ""
                                                , ""
                                                , false
                                                , false));
                                    }
                                    lists.get(index).add(new HomePageModel(2, documentSnapshot.get("layout_title").toString(), documentSnapshot.get("layout_background").toString(), horizontalProductScrollModelList, viewAllProductList));
                                } else if ((long) documentSnapshot.get("view_type") == 3) {

                                    List<HorizontalProductScrollModel> gridLayoutModelList = new ArrayList<>();
                                    ArrayList<String> productIds = (ArrayList<String>) documentSnapshot.get("products");
                                    for (String productId : productIds) {
                                        gridLayoutModelList.add(new HorizontalProductScrollModel(productId
                                                , ""
                                                , ""
                                                , ""
                                                , ""));
                                    }
                                    lists.get(index).add(new HomePageModel(3, documentSnapshot.get("layout_title").toString(), documentSnapshot.get("layout_background").toString(), gridLayoutModelList));
                                }
                            }
                            HomePageAdapter homePageAdapter = new HomePageAdapter(lists.get(index));
                            homePageRecyclerView.setAdapter(homePageAdapter);
                            homePageAdapter.notifyDataSetChanged();
                            HomeFragment.swipeRefreshLayout.setRefreshing(false);
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void loadWishlist(final Context context, final Dialog dialog, final boolean loadProductData) {
        wishlist.clear();
        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_WISHLIST")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    for (long x = 0; x < (long) task.getResult().get("list_size"); x++) {
                        wishlist.add(task.getResult().get("product_ID_" + x).toString());

                        if (DBqueries.wishlist.contains(productID)) {
                            ALREADY_ADDED_TO_WISHLIST = true;
                            if (addToWishlistBtn != null) {
                                addToWishlistBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.colorPrimary));
                            }
                        } else {
                            if (addToWishlistBtn != null) {
                                addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                                ALREADY_ADDED_TO_WISHLIST = false;
                            }
                        }
                        if (loadProductData) {
                            wishlistModelList.clear();
                            final String productId = task.getResult().get("product_ID_" + x).toString();
                            firebaseFirestore.collection("PRODUCTS").document(task.getResult().get("product_ID_" + x).toString())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        final DocumentSnapshot documentSnapshot = task.getResult();
                                        FirebaseFirestore.getInstance().collection("PRODUCTS").document(productId).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {

                                                            if (task.getResult().getDocuments().size() < (long) documentSnapshot.get("stock_quantity")) {
                                                                wishlistModelList.add(new WishlistModel(productId, documentSnapshot.get("product_image_1").toString()
                                                                        , documentSnapshot.get("product_title").toString()
                                                                        , (long) documentSnapshot.get("free_coupons")
                                                                        , documentSnapshot.get("average_rating").toString()
                                                                        , (long) documentSnapshot.get("total_ratings")
                                                                        , documentSnapshot.get("product_price").toString()
                                                                        , documentSnapshot.get("cutted_price").toString()
                                                                        , (boolean) documentSnapshot.get("COD")
                                                                        , true));
                                                            } else {
                                                                wishlistModelList.add(new WishlistModel(productId, documentSnapshot.get("product_image_1").toString()
                                                                        , documentSnapshot.get("product_title").toString()
                                                                        , (long) documentSnapshot.get("free_coupons")
                                                                        , documentSnapshot.get("average_rating").toString()
                                                                        , (long) documentSnapshot.get("total_ratings")
                                                                        , documentSnapshot.get("product_price").toString()
                                                                        , documentSnapshot.get("cutted_price").toString()
                                                                        , (boolean) documentSnapshot.get("COD")
                                                                        , false));
                                                            }

                                                            WishlistFragment.wishlistAdapter.notifyDataSetChanged();

                                                        } else {
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                            //////error
                                                        }
                                                    }
                                                });

                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });
                        }
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });
    }

    public static void removeFromWishlist(final int index, final Context context) {
        final String removedProductId = wishlist.get(index);
        wishlist.remove(index);
        Map<String, Object> updateWishlist = new HashMap<>();

        for (int x = 0; x < wishlist.size(); x++) {
            updateWishlist.put("product_ID_" + x, wishlist.get(x));
        }
        updateWishlist.put("list_size", (long) wishlist.size());

        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_WISHLIST")
                .set(updateWishlist).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    if (wishlistModelList.size() != 0) {
                        wishlistModelList.remove(index);
                        WishlistFragment.wishlistAdapter.notifyDataSetChanged();
                    }
                    ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST = false;
                    Toast.makeText(context, "Removed Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    if (addToWishlistBtn != null) {
                        addToWishlistBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.colorPrimary));
                    }
                    wishlist.add(index, removedProductId);
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }

                RUNNING_WISHLIST_QUERY = false;
            }
        });
    }

    public static void loadRatingList(final Context context) {
        if (!RUNNING_RATING_QUERY) {
            RUNNING_RATING_QUERY = true;
            myRatedIds.clear();
            myRating.clear();
            firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_RATINGS")
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        List<String> orderProductIds = new ArrayList<>();

                        for (int x = 0; x < myOrderItemModelList.size(); x++) {
                            orderProductIds.add(myOrderItemModelList.get(x).getProductId());
                        }

                        for (long x = 0; x < (long) task.getResult().get("list_size"); x++) {
                            myRatedIds.add(task.getResult().get("product_ID_" + x).toString());
                            myRating.add((long) task.getResult().get("rating_" + x));

                            if (task.getResult().get("product_ID_" + x).toString().equals(productID)) {
                                initialRating = Integer.parseInt(String.valueOf((long) task.getResult().get("rating_" + x))) - 1;
                                if (rateNowContainer != null) {
                                    setRating(initialRating);
                                }
                            }

                            if (orderProductIds.contains(task.getResult().get("product_ID_" + x).toString())) {
                                myOrderItemModelList.get(orderProductIds.indexOf(task.getResult().get("product_ID_" + x).toString())).setRating(Integer.parseInt(String.valueOf((long) task.getResult().get("rating_" + x))) - 1);
                            }
                        }
                        if (MyOrdersFragment.myOrderAdapter != null) {
                            MyOrdersFragment.myOrderAdapter.notifyDataSetChanged();
                        }

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                    }
                    RUNNING_RATING_QUERY = false;
                }
            });
        }
    }

    public static void loadCartList(final Context context, final Dialog dialog, final boolean loadProductData, final TextView badgeCount, final TextView totalCartAmount) {
        cartList.clear();

        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    for (long x = 0; x < (long) task.getResult().get("list_size"); x++) {
                        cartList.add(task.getResult().get("product_ID_" + x).toString());

                        if (DBqueries.cartList.contains(productID)) {
                            ALREADY_ADDED_TO_CART = true;
                        } else {
                            ALREADY_ADDED_TO_CART = false;
                        }

                        if (loadProductData) {
                            cartItemModelList.clear();
                            final String productId = task.getResult().get("product_ID_" + x).toString();
                            firebaseFirestore.collection("PRODUCTS").document(task.getResult().get("product_ID_" + x).toString())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {


                                        int index = 0;
                                        if (cartList.size() >= 2) {
                                            index = cartList.size() - 2;
                                        }

                                        final DocumentSnapshot documentSnapshot = task.getResult();
                                        FirebaseFirestore.getInstance().collection("PRODUCTS").document(productId).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {

                                                            if (task.getResult().getDocuments().size() < (long) documentSnapshot.get("stock_quantity")) {
                                                                cartItemModelList.add(new CartItemModel(documentSnapshot.getBoolean("COD"), CartItemModel.CART_ITEM, productId,
                                                                        documentSnapshot.get("product_image_1").toString()
                                                                        , documentSnapshot.get("product_title").toString()
                                                                        , (long) documentSnapshot.get("free_coupons")
                                                                        , documentSnapshot.get("product_price").toString()
                                                                        , documentSnapshot.get("cutted_price").toString()
                                                                        , (long) 1
                                                                        , (long) documentSnapshot.get("offers_applied")
                                                                        , (long) 0
                                                                        , true
                                                                        , (long) documentSnapshot.get("max_quantity")
                                                                        , (long) documentSnapshot.get("stock_quantity")));
                                                            } else {
                                                                cartItemModelList.add(new CartItemModel(documentSnapshot.getBoolean("COD"), CartItemModel.CART_ITEM, productId,
                                                                        documentSnapshot.get("product_image_1").toString()
                                                                        , documentSnapshot.get("product_title").toString()
                                                                        , (long) documentSnapshot.get("free_coupons")
                                                                        , documentSnapshot.get("product_price").toString()
                                                                        , documentSnapshot.get("cutted_price").toString()
                                                                        , (long) 1
                                                                        , (long) documentSnapshot.get("offers_applied")
                                                                        , (long) 0
                                                                        , false
                                                                        , (long) documentSnapshot.get("max_quantity")
                                                                        , (long) documentSnapshot.get("stock_quantity")));
                                                            }

                                                            if (cartList.size() == 1) {
                                                                cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));
                                                                LinearLayout parent = (LinearLayout) totalCartAmount.getParent().getParent();
                                                                parent.setVisibility(View.VISIBLE);

                                                            }

                                                            if (cartList.size() == 0) {
                                                                cartItemModelList.clear();
                                                            }
                                                            MyCartFragment.cartAdapter.notifyDataSetChanged();

                                                        } else {
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                            //////error
                                                        }
                                                    }
                                                });
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });
                        }
                    }
                    if (cartList.size() != 0) {
                        badgeCount.setVisibility(View.VISIBLE);
                    } else {
                        badgeCount.setVisibility(View.INVISIBLE);

                    }
                    if (DBqueries.cartList.size() < 99) {
                        badgeCount.setText(String.valueOf(DBqueries.cartList.size()));
                    } else {
                        badgeCount.setText("99+");
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });
    }

    public static void removeFromCart(final int index, final Context context, final TextView totalCartAmount) {
        final String removedProductId = cartList.get(index);
        cartList.remove(index);
        Map<String, Object> updateCartList = new HashMap<>();

        for (int x = 0; x < cartList.size(); x++) {
            updateCartList.put("product_ID_" + x, cartList.get(x));
        }
        updateCartList.put("list_size", (long) cartList.size());

        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                .set(updateCartList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    if (cartItemModelList.size() != 0) {
                        cartItemModelList.remove(index);
                        MyCartFragment.cartAdapter.notifyDataSetChanged();
                    }

                    if (cartList.size() == 0) {
                        LinearLayout parent = (LinearLayout) totalCartAmount.getParent().getParent();
                        parent.setVisibility(View.GONE);
                        cartItemModelList.clear();
                    }

                    Toast.makeText(context, "Removed Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    cartList.add(index, removedProductId);
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }

                RUNNING_CART_QUERY = false;
            }
        });
    }

    public static void loadAddresses(final Context context, final Dialog loadingDialog, final boolean gotoDeliveryActivity) {

        addressesModelList.clear();
        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_ADDRESSES")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Intent deliveryIntent = null;
                    if ((long) task.getResult().get("list_size") == 0) {
                        deliveryIntent = new Intent(context, AddAddressActivity.class);
                        deliveryIntent.putExtra("INTENT", "deliveryIntent");
                    } else {

                        for (int x = 1; x < (long) task.getResult().get("list_size") + 1; x++) {

                            addressesModelList.add(new AddressesModel(task.getResult().getBoolean("selected_" + x)
                                    , task.getResult().getString("city_" + x)
                                    , task.getResult().getString("locality_" + x)
                                    , task.getResult().getString("flat_no_" + x)
                                    , task.getResult().getString("pincode_" + x)
                                    , task.getResult().getString("landmark_" + x)
                                    , task.getResult().getString("name_" + x)
                                    , task.getResult().getString("mobile_no_" + x)
                                    , task.getResult().getString("alternate_mobile_no_" + x)
                                    , task.getResult().getString("state_" + x)));

                            if (task.getResult().getBoolean("selected_" + x)) {
                                selectedAddress = Integer.parseInt(String.valueOf(x - 1));
                            }
                        }
                        if (gotoDeliveryActivity) {
                            deliveryIntent = new Intent(context, DeliveryActivity.class);
                        }
                    }
                    if (gotoDeliveryActivity) {
                        context.startActivity(deliveryIntent);
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });
    }

    public static void loadRewards(final Context context, final Dialog loadingDialog, final boolean onRewardFragment) {
        rewardModelList.clear();

        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final Date lastSeenDate = task.getResult().getDate("Last seen");

                            firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_REWARDS")
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                            if (documentSnapshot.get("type").toString().equals("Discount") && lastSeenDate.before(documentSnapshot.getTimestamp("validity").toDate())) {
                                                rewardModelList.add(new RewardModel(documentSnapshot.getId(), documentSnapshot.get("type").toString()
                                                        , documentSnapshot.get("lower_limit").toString()
                                                        , documentSnapshot.get("upper_limit").toString()
                                                        , documentSnapshot.get("percentage").toString()
                                                        , documentSnapshot.get("body").toString()
                                                        , documentSnapshot.getTimestamp("validity").toDate()
                                                        , (boolean) documentSnapshot.get("already_used")));

                                            } else if (documentSnapshot.get("type").toString().equals("Flat Rs.* OFF") && lastSeenDate.before(documentSnapshot.getTimestamp("validity").toDate())) {
                                                rewardModelList.add(new RewardModel(documentSnapshot.getId(), documentSnapshot.get("type").toString()
                                                        , documentSnapshot.get("lower_limit").toString()
                                                        , documentSnapshot.get("upper_limit").toString()
                                                        , documentSnapshot.get("amount").toString()
                                                        , documentSnapshot.get("body").toString()
                                                        , documentSnapshot.getTimestamp("validity").toDate()
                                                        , (boolean) documentSnapshot.get("already_used")));
                                            }
                                        }
                                        if (onRewardFragment) {
                                            MyRewardsFragment.rewardsAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                    }
                                    loadingDialog.dismiss();
                                }

                            });

                        } else {
                            loadingDialog.dismiss();
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    public static void loadOrders(final Context context, @Nullable final MyOrderAdapter myOrderAdapter, final Dialog loadingDialog) {
        myOrderItemModelList.clear();
        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").orderBy("time", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {

                        firebaseFirestore.collection("ORDERS").document(documentSnapshot.getString("order_id")).collection("ORDER_ITEMS").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {

                                    for (DocumentSnapshot orderItems : task.getResult().getDocuments()) {

                                        MyOrderItemModel myOrderItemModel = new MyOrderItemModel(orderItems.getString("PRODUCT ID"), orderItems.getString("ORDER STATUS"), orderItems.getString("ADDRESS"), orderItems.getString("COUPON ID"), orderItems.getString("CUTTED PRICE"), orderItems.getDate("ORDERED DATE"), orderItems.getDate("PACKED DATE"), orderItems.getDate("SHIPPED DATE"), orderItems.getDate("DELIVERED DATE"), orderItems.getDate("CANCELLED DATE"), orderItems.getString("DISCOUNTED PRICE"), orderItems.getLong("FREE COUPONS"), orderItems.getString("FULLNAME"), orderItems.getString("ORDER ID"), orderItems.getString("PAYMENT METHOD"), orderItems.getString("PINCODE"), orderItems.getString("PRODUCT PRICE"), orderItems.getLong("PRODUCT QUANTITY"), orderItems.getString("USER ID"), orderItems.getString("PRODUCT IMAGE"), orderItems.getString("PRODUCT TITLE"), orderItems.getString("DELIVERY PRICE"), orderItems.getBoolean("CANCELLATION REQUESTED"));

                                        myOrderItemModelList.add(myOrderItemModel);

                                    }

                                    loadRatingList(context);
                                    if (myOrderAdapter != null) {
                                        myOrderAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                }
                                loadingDialog.dismiss();
                            }
                        });
                    }
                } else {
                    loadingDialog.dismiss();
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void checkNotifications(boolean remove, @Nullable final TextView notifyCount) {

        if (remove) {
            registration.remove();
        } else {
            registration = firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_NOTIFICATIONS")
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@androidx.annotation.Nullable DocumentSnapshot documentSnapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                notificationModelList.clear();
                                int unread = 0;
                                for (long x = 0; x < (long) documentSnapshot.get("list_size"); x++) {

                                    notificationModelList.add(0, new NotificationModel(documentSnapshot.get("Image_" + x).toString()
                                            , documentSnapshot.get("Body_" + x).toString()
                                            , documentSnapshot.getBoolean("Readed_" + x)));
                                    if (!documentSnapshot.getBoolean("Readed_" + x)) {
                                        unread++;
                                        if (notifyCount != null) {
                                            if (unread > 0) {

                                                notifyCount.setVisibility(View.VISIBLE);
                                                if (unread < 99) {
                                                    notifyCount.setText(String.valueOf(unread));
                                                } else {
                                                    notifyCount.setText("99+");
                                                }
                                            } else {
                                                notifyCount.setVisibility(View.INVISIBLE);

                                            }
                                        }
                                    }


                                }

                                if (NotificationActivity.adapter != null) {
                                    NotificationActivity.adapter.notifyDataSetChanged();
                                }
                            }

                        }
                    });
        }


    }

    public static void clearData() {
        categoryModelList.clear();
        lists.clear();
        loadedCategoriesNames.clear();
        wishlist.clear();
        wishlistModelList.clear();
        cartList.clear();
        cartItemModelList.clear();
        myRatedIds.clear();
        myRating.clear();
        addressesModelList.clear();
        rewardModelList.clear();
        myOrderItemModelList.clear();
    }
}
