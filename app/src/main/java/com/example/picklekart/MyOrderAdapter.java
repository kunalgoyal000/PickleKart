package com.example.picklekart;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firestore.v1.StructuredQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.picklekart.ProductDetailsActivity.productID;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder> {

    List<MyOrderItemModel> orderItemModelList;
    private Dialog loadingDialog;
    private SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(List<MyOrderItemModel> orderItemModelList, Dialog loadingDialog) {
        this.orderItemModelList = orderItemModelList;
        this.loadingDialog = loadingDialog;
    }

    @NonNull
    @Override
    public MyOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_order_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyOrderAdapter.ViewHolder holder, int position) {

        String resource = orderItemModelList.get(position).getProductImage();
        String productId = orderItemModelList.get(position).getProductId();
        int rating = orderItemModelList.get(position).getRating();
        String title = orderItemModelList.get(position).getProductTitle();
        String orderStatus = orderItemModelList.get(position).getOrderStatus();
        Date date;
        switch (orderStatus) {

            case "Ordered":
                date = orderItemModelList.get(position).getOrderedDate();
                break;
            case "Packed":
                date = orderItemModelList.get(position).getPackedDate();
                break;
            case "Shipped":
                date = orderItemModelList.get(position).getShippedDate();
                break;
            case "Delivered":
                date = orderItemModelList.get(position).getDeliveredDate();
                break;
            case "Cancelled":
                date = orderItemModelList.get(position).getCancelledDate();
                break;
            default:
                date = orderItemModelList.get(position).getCancelledDate();


        }
        //   int rating=orderItemModelList.get(position).getRating();

        holder.setData(resource, title, orderStatus, date, rating, productId, position);
    }

    @Override
    public int getItemCount() {
        return orderItemModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private ImageView orderIndicator;
        private TextView productTitle;
        private TextView deliveryStatus;
        private LinearLayout rateNowContainer;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.product_image);
            orderIndicator = itemView.findViewById(R.id.order_indicator);
            productTitle = itemView.findViewById(R.id.product_title);
            deliveryStatus = itemView.findViewById(R.id.order_delivered_date);
            rateNowContainer = itemView.findViewById(R.id.rate_now_container);

        }

        private void setData(String resource, String title, String orderStatus, Date date, final int rating, final String productId, final int position) {
            Glide.with(itemView.getContext()).load(resource).into(productImage);
            productTitle.setText(title);
            if (orderStatus.equals("Cancelled")) {
                orderIndicator.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.colorPrimary)));
            } else {
                orderIndicator.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.successGreen)));

            }

            simpleDateFormat = new SimpleDateFormat("EEE, dd MMM YYYY hh:mm aa");

            deliveryStatus.setText(orderStatus + " on " + String.valueOf(simpleDateFormat.format(date)));


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent orderDetailsIntent = new Intent(itemView.getContext(), OrderDetailsActivity.class);
                    orderDetailsIntent.putExtra("Position", position);
                    itemView.getContext().startActivity(orderDetailsIntent);
                }
            });
            ////////Rating layout

            setRating(rating);
            for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
                final int starPosition = x;

                rateNowContainer.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadingDialog.show();
                        setRating(starPosition);
                        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("PRODUCTS").document(productId);

                        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Object>() {
                            @Nullable
                            @Override
                            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                DocumentSnapshot documentSnapshot = transaction.get(documentReference);

                                if (rating != 0) {
                                    Long increase = documentSnapshot.getLong(starPosition + 1 + "_star") + 1;
                                    Long decrease = documentSnapshot.getLong(rating + 1 + "_star") - 1;
                                    transaction.update(documentReference, starPosition + 1 + "_star", increase);
                                    transaction.update(documentReference, starPosition + 1 + "_star", decrease);

                                } else {
                                    Long increase = documentSnapshot.getLong(starPosition + 1 + "_star") + 1;
                                    transaction.update(documentReference, starPosition + 1 + "_star", increase);
                                }
                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Object>() {
                            @Override
                            public void onSuccess(Object o) {

                                Map<String, Object> myRating = new HashMap<>();

                                if (DBqueries.myRatedIds.contains(productID)) {
                                    myRating.put("rating_" + DBqueries.myRatedIds.indexOf(productID), (long) starPosition + 1);

                                } else {
                                    myRating.put("list_size", (long) DBqueries.myRatedIds.size() + 1);
                                    myRating.put("product_ID_" + DBqueries.myRatedIds.size(), productID);
                                    myRating.put("rating_" + DBqueries.myRatedIds.size(), (long) starPosition + 1);
                                }


                                FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_RATINGS")
                                        .update(myRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            DBqueries.myOrderItemModelList.get(position).setRating(starPosition);
                                            if (DBqueries.myRatedIds.contains(productId)) {
                                                DBqueries.myRating.set(DBqueries.myRatedIds.indexOf(productId), Long.parseLong(String.valueOf(starPosition + 1)));
                                            } else {
                                                DBqueries.myRatedIds.add(productId);
                                                DBqueries.myRating.add(Long.parseLong(String.valueOf(starPosition + 1)));
                                            }
                                        } else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(itemView.getContext(), error, Toast.LENGTH_SHORT).show();
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismiss();
                            }
                        });
                    }
                });
            }

            //////////Rating layout
        }

        private void setRating(int starPosition) {
            for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
                ImageView starBtn = (ImageView) rateNowContainer.getChildAt(x);
                starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#bebebe")));

                if (x <= starPosition) {
                    starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffbb00")));

                }

            }

        }


    }
}