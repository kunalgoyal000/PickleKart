package com.example.picklekart;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import static com.example.picklekart.ProductDetailsActivity.RUNNING_WISHLIST_QUERY;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private boolean fromSearch;
    List<WishlistModel> wishlistModelList;
    private boolean wishlist;
    private int lastPosition = -1;

    public boolean isFromSearch() {
        return fromSearch;
    }

    public void setFromSearch(boolean fromSearch) {
        this.fromSearch = fromSearch;
    }

    public WishlistAdapter(List<WishlistModel> wishlistModelList, boolean wishlist) {
        this.wishlistModelList = wishlistModelList;
        this.wishlist = wishlist;
    }

    public List<WishlistModel> getWishlistModelList() {
        return wishlistModelList;
    }

    public void setWishlistModelList(List<WishlistModel> wishlistModelList) {
        this.wishlistModelList = wishlistModelList;
    }

    @NonNull
    @Override
    public WishlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wishlist_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistAdapter.ViewHolder holder, int position) {
        String productId = wishlistModelList.get(position).getProductId();
        String resource = wishlistModelList.get(position).getProductImage();
        String title = wishlistModelList.get(position).getProductTitle();
        long freeCouponNum = wishlistModelList.get(position).getFreeCoupons();
        String rating = wishlistModelList.get(position).getRating();
        long totalRatings = wishlistModelList.get(position).getTotalRatings();
        String productPrice = wishlistModelList.get(position).getProductPrice();
        String cuttedPrice = wishlistModelList.get(position).getCuttedPrice();
        boolean payMethod = wishlistModelList.get(position).isCOD();
        boolean inStock = wishlistModelList.get(position).isInStock();
        holder.setData(productId, resource, title, freeCouponNum, rating, totalRatings, productPrice, cuttedPrice, payMethod, position, inStock);

        if (lastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
            holder.itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return wishlistModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private ImageView couponIcon;
        private TextView productTitle;
        private TextView freeCoupons;
        private TextView productPrice;
        private TextView cuttedPrice;
        private TextView rating;
        private View priceCut;
        private TextView totalRating;
        private TextView paymentMethod;
        private ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productTitle = itemView.findViewById(R.id.product_title);
            productPrice = itemView.findViewById(R.id.product_price);
            rating = itemView.findViewById(R.id.tv_product_rating_miniview);
            cuttedPrice = itemView.findViewById(R.id.cutted_price);
            totalRating = itemView.findViewById(R.id.total_ratings_miniview);
            couponIcon = itemView.findViewById(R.id.coupon_icon);
            freeCoupons = itemView.findViewById(R.id.free_coupon);
            paymentMethod = itemView.findViewById(R.id.payment_method);
            priceCut = itemView.findViewById(R.id.price_cut);
            deleteBtn = itemView.findViewById(R.id.delete_button);
        }

        private void setData(final String productId, String resource, String title, long freeCouponNum, String averageRate, long totalRatingsNum, String productPriceValue, String cuttedPriceValue, boolean COD, final int index, final boolean inStock) {

            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions()).placeholder(R.drawable.placeholder_icon).into(productImage);
            productTitle.setText(title);

            if (freeCouponNum != 0 && inStock) {
                if (freeCouponNum == 1) {
                    couponIcon.setVisibility(View.VISIBLE);
                    freeCoupons.setVisibility(View.VISIBLE);
                    freeCoupons.setText("Free " + freeCouponNum + " Coupon");
                } else {
                    freeCoupons.setText("Free " + freeCouponNum + " Coupons");
                }
            } else {
                couponIcon.setVisibility(View.INVISIBLE);
                freeCoupons.setVisibility(View.INVISIBLE);
            }

            LinearLayout linearLayout = (LinearLayout) rating.getParent();

            if (inStock) {

                rating.setVisibility(View.VISIBLE);
                totalRating.setVisibility(View.VISIBLE);
                productPrice.setTextColor(Color.parseColor("#000000"));
                cuttedPrice.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                rating.setText(averageRate);
                totalRating.setText("(" + totalRatingsNum + ")" + "Ratings");
                productPrice.setText("Rs. " + productPriceValue + "/-");
                cuttedPrice.setText("Rs. " + cuttedPriceValue + "/-");
                priceCut.setVisibility(View.VISIBLE);
                if (COD) {
                    paymentMethod.setVisibility(View.VISIBLE);
                } else {
                    paymentMethod.setVisibility(View.INVISIBLE);

                }

            } else {
                rating.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                totalRating.setVisibility(View.INVISIBLE);
                paymentMethod.setVisibility(View.INVISIBLE);
                productPrice.setText("Out of Stock");
                productPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                cuttedPrice.setVisibility(View.INVISIBLE);
                priceCut.setVisibility(View.INVISIBLE);
            }

            if (wishlist) {
                deleteBtn.setVisibility(View.VISIBLE);
            } else {
                deleteBtn.setVisibility(View.GONE);
            }
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!RUNNING_WISHLIST_QUERY) {
                        RUNNING_WISHLIST_QUERY = true;
                        DBqueries.removeFromWishlist(index, itemView.getContext());
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (fromSearch) {
                        ProductDetailsActivity.fromSearch = true;
                    }
                    Intent productDetailsIntent = new Intent(itemView.getContext(), ProductDetailsActivity.class);
                    productDetailsIntent.putExtra("PRODUCT_ID", productId);
                    itemView.getContext().startActivity(productDetailsIntent);
                }
            });


        }
    }
}
