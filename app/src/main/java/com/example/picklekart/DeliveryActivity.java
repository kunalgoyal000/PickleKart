package com.example.picklekart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.picklekart.DBqueries.cartList;

public class DeliveryActivity extends AppCompatActivity {

    public static List<CartItemModel> cartItemModelList;
    private RecyclerView deliveryRecyclerView;
    private Button changeORAddNewAddressBtn;

    public static final int SELECT_ADDRESS = 0;
    private TextView totalCartAmount;

    private TextView fullName;
    private String name, mobileNo;
    private TextView fullAddress;
    private TextView pincode;
    private String paymentMethod = "PAYTM";
    private Button continueBtn;
    public static Dialog loadingDialog;
    private Dialog paymentMethodDialog;
    private ImageButton paytm;
    private ImageButton cod;
    private TextView codBtnTitle;
    private View dividerPayment;

    private ConstraintLayout orderConfirmationLayout;
    private ImageButton continueShoppingBtn;
    private TextView orderId;
    private boolean successResponse = false;
    public static boolean fromCart = false;
    private String order_id;
    public static boolean codOrderConfirmed = false;
    private FirebaseFirestore firebaseFirestore;

    public static boolean getQtyIDs = true;
    public static CartAdapter cartAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        totalCartAmount = findViewById(R.id.total_cart_amount);
        continueBtn = findViewById(R.id.cart_continue_btn);
        orderConfirmationLayout = findViewById(R.id.order_confirmation_layout);
        orderId = findViewById(R.id.order_id);
        continueShoppingBtn = findViewById(R.id.continue_shopping_btn);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Delivery");


        /////////// Loading Dialog

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));

        //////////// Loading Dialog


        ///////////Payment  Dialog

        paymentMethodDialog = new Dialog(this);
        paymentMethodDialog.setContentView(R.layout.payment_method);
        paymentMethodDialog.setCancelable(true);
        paymentMethodDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paymentMethodDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        paytm = paymentMethodDialog.findViewById(R.id.paytm);
        cod = paymentMethodDialog.findViewById(R.id.cod_btn);
        codBtnTitle = paymentMethodDialog.findViewById(R.id.cod_btn_title);
        dividerPayment = paymentMethodDialog.findViewById(R.id.divider_payment);
        //////////// Payment Dialog
        firebaseFirestore = FirebaseFirestore.getInstance();
        getQtyIDs = true;
        order_id = UUID.randomUUID().toString().substring(0, 28);

        deliveryRecyclerView = findViewById(R.id.delivery_recyclerview);
        fullName = findViewById(R.id.name);
        fullAddress = findViewById(R.id.address);
        pincode = findViewById(R.id.pincode);

        changeORAddNewAddressBtn = findViewById(R.id.change_or_add_address_btn);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        deliveryRecyclerView.setLayoutManager(linearLayoutManager);

        cartAdapter = new CartAdapter(cartItemModelList, totalCartAmount, false);
        deliveryRecyclerView.setAdapter(cartAdapter);

        cartAdapter.notifyDataSetChanged();

        changeORAddNewAddressBtn.setVisibility(View.VISIBLE);
        changeORAddNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getQtyIDs = false;
                Intent myAddressesIntent = new Intent(DeliveryActivity.this, MyAddressesActivity.class);
                myAddressesIntent.putExtra("MODE", SELECT_ADDRESS);
                startActivity(myAddressesIntent);
            }
        });


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean allProductsAvailable = true;

                for (CartItemModel cartItemModel : cartItemModelList) {
                    if (cartItemModel.isQtyError()) {
                        allProductsAvailable = false;
                        break;
                    }
                    if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                        if (!cartItemModel.isCOD()) {
                            cod.setEnabled(false);
                            cod.setAlpha(0.5f);
                            codBtnTitle.setAlpha(0.5f);

                            break;
                        } else {
                            cod.setEnabled(true);
                            cod.setAlpha(1f);
                            codBtnTitle.setAlpha(1f);
                        }
                    }
                }
                if (allProductsAvailable) {
                    paymentMethodDialog.show();
                }
            }
        });


        cod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentMethod = "COD";
                placeOrderDetails();
            }
        });

        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentMethod = "PAYTM";
                placeOrderDetails();
            }

        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        ////accessing quantity
        if (getQtyIDs) {
            loadingDialog.show();
            for (int x = 0; x < cartItemModelList.size() - 1; x++) {

                for (int y = 0; y < cartItemModelList.get(x).getProductQuantity(); y++) {
                    final String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);
                    Map<String, Object> timeStamp = new HashMap<>();
                    timeStamp.put("time", FieldValue.serverTimestamp());
                    final int finalX = x;
                    final int finalY = y;
                    firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(quantityDocumentName)
                            .set(timeStamp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                cartItemModelList.get(finalX).getQtyIDs().add(quantityDocumentName);

                                if (finalY + 1 == cartItemModelList.get(finalX).getProductQuantity()) {

                                    firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(finalX).getProductID()).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).limit(cartItemModelList.get(finalX).getStockQuantity()).get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        List<String> serverQuantity = new ArrayList<>();

                                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                            serverQuantity.add(queryDocumentSnapshot.getId());
                                                        }

                                                        long availableQty = 0;
                                                        boolean noLongerAvailable = true;

                                                        for (String qtyId : cartItemModelList.get(finalX).getQtyIDs()) {
                                                            cartItemModelList.get(finalX).setQtyError(false);

                                                            if (!serverQuantity.contains(qtyId)) {

                                                                if (noLongerAvailable) {
                                                                    cartItemModelList.get(finalX).setInStock(false);
                                                                } else {
                                                                    cartItemModelList.get(finalX).setQtyError(true);
                                                                    cartItemModelList.get(finalX).setMaxQuantity(availableQty);
                                                                    Toast.makeText(DeliveryActivity.this, "Sorry ! All products may not be available in required quantity....", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                availableQty++;
                                                                noLongerAvailable = false;
                                                            }
                                                        }

                                                        cartAdapter.notifyDataSetChanged();
                                                    } else {
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                                        //////error
                                                    }
                                                    loadingDialog.dismiss();
                                                }
                                            });
                                }
                            } else {
                                loadingDialog.dismiss();
                                String error = task.getException().getMessage();
                                Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        } else {
            getQtyIDs = true;
        }
        ////accessing quantity

        name = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getName();
        mobileNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getMobileNo();
        if (DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo().equals("")) {

            fullName.setText(name + " - " + mobileNo);
        } else {
            fullName.setText(name + " - " + mobileNo + " or " + DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo());

        }
        String flatNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getFlatNo();
        String locality = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLocality();
        String landmark = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLandmark();
        String city = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getCity();
        String state = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getState();
        if (landmark.equals("")) {
            fullAddress.setText(flatNo + "," + locality + "," + city + "," + state);

        } else {
            fullAddress.setText(flatNo + "," + locality + "," + landmark + "," + city + "," + state);
        }
        pincode.setText(DBqueries.addressesModelList.get(DBqueries.selectedAddress).getPincode());

        if (codOrderConfirmed) {
            showConfirmationLayout();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadingDialog.dismiss();
        if (getQtyIDs) {

            for (int x = 0; x < cartItemModelList.size() - 1; x++) {
                if (!successResponse) {
                    for (final String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                        final int finalX = x;
                        firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (qtyID.equals(cartItemModelList.get(finalX).getQtyIDs().get(cartItemModelList.get(finalX).getQtyIDs().size() - 1))) {
                                            cartItemModelList.get(finalX).getQtyIDs().clear();
                                        }
                                    }
                                });
                    }
                } else {
                    cartItemModelList.get(x).getQtyIDs().clear();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (successResponse) {
            finish();
            return;
        }
        super.onBackPressed();

    }

    private void showConfirmationLayout() {

        String SMS_API = "https://www.fast2sms.com/dev/bulk";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SMS_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authorization", "YZN1fAlIwQUJkpj34azWtbyrveX0D65GHKVTM2PdmxFogqiunEqpHSOavz12J9eDLdRVyU5ZkYKmPFo3");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> body = new HashMap<>();
                body.put("sender_id", "FSTSMS");
                body.put("language", "english");
                body.put("route", "qt");
                body.put("numbers", mobileNo);
                body.put("message", "16423");
                body.put("variables", "{#FF#}");
                body.put("variables_values", order_id);

                return body;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
        requestQueue.add(stringRequest);


        successResponse = true;
        codOrderConfirmed = false;
        getQtyIDs = false;

        for (int x = 0; x < cartItemModelList.size() - 1; x++) {

            for (String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).update("user_ID", FirebaseAuth.getInstance().getUid());

            }

        }


        if (MainActivity.mainActivity != null) {
            MainActivity.mainActivity.finish();
            MainActivity.mainActivity = null;
            MainActivity.showCart = false;
        } else {
            MainActivity.resetMainActivity = true;
        }
        if (ProductDetailsActivity.productDetailsActivity != null) {
            ProductDetailsActivity.productDetailsActivity.finish();
            ProductDetailsActivity.productDetailsActivity = null;
        }


        if (fromCart) {
            loadingDialog.show();
            Map<String, Object> updateCartList = new HashMap<>();
            long cartListSize = 0;
            final List<Integer> indexList = new ArrayList<>();
            for (int x = 0; x < cartList.size(); x++) {
                if (!cartItemModelList.get(x).isInStock()) {
                    updateCartList.put("product_ID_" + cartListSize, cartItemModelList.get(x).getProductID());
                    cartListSize++;
                } else {
                    indexList.add(x);
                }
            }
            updateCartList.put("list_size", cartListSize);


            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                    .set(updateCartList).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        for (int x = 0; x < indexList.size(); x++) {
                            DBqueries.cartList.remove(indexList.get(x).intValue());
                            DBqueries.cartItemModelList.remove(indexList.get(x).intValue());
                            DBqueries.cartItemModelList.remove(DBqueries.cartItemModelList.size() - 1);
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                }
            });
        }
        continueBtn.setEnabled(false);
        changeORAddNewAddressBtn.setEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        orderId.setText("Order ID " + order_id);
        orderConfirmationLayout.setVisibility(View.VISIBLE);
        continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void placeOrderDetails() {
        String userID = FirebaseAuth.getInstance().getUid();
        loadingDialog.show();
        for (CartItemModel cartItemModel : cartItemModelList) {
            if (cartItemModel.getType() == cartItemModel.CART_ITEM) {

                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("ORDER ID", order_id);
                orderDetails.put("PRODUCT ID", cartItemModel.getProductID());
                orderDetails.put("PRODUCT IMAGE", cartItemModel.getProductImage());
                orderDetails.put("PRODUCT TITLE", cartItemModel.getProductTitle());
                orderDetails.put("USER ID", userID);
                orderDetails.put("PRODUCT QUANTITY", cartItemModel.getProductQuantity());
                if (cartItemModel.getCuttedPrice() != null) {
                    orderDetails.put("CUTTED PRICE", cartItemModel.getCuttedPrice());
                } else {
                    orderDetails.put("CUTTED PRICE", "");

                }
                orderDetails.put("PRODUCT PRICE", cartItemModel.getProductPrice());
                if (cartItemModel.getSelectedCouponId() != null) {
                    orderDetails.put("COUPON ID", cartItemModel.getSelectedCouponId());
                } else {
                    orderDetails.put("COUPON ID", "");

                }
                if (cartItemModel.getDiscountedPrice() != null) {
                    orderDetails.put("DISCOUNTED PRICE", cartItemModel.getDiscountedPrice());
                } else {
                    orderDetails.put("DISCOUNTED PRICE", "");

                }
                orderDetails.put("ORDERED DATE", FieldValue.serverTimestamp());
                orderDetails.put("PACKED DATE", FieldValue.serverTimestamp());
                orderDetails.put("SHIPPED DATE", FieldValue.serverTimestamp());
                orderDetails.put("DELIVERED DATE", FieldValue.serverTimestamp());
                orderDetails.put("CANCELLED DATE", FieldValue.serverTimestamp());
                orderDetails.put("ORDER STATUS", "Ordered");
                orderDetails.put("PAYMENT METHOD", paymentMethod);
                orderDetails.put("ADDRESS", fullAddress.getText());
                orderDetails.put("FULLNAME", fullName.getText());
                orderDetails.put("PINCODE", pincode.getText());
                orderDetails.put("FREE COUPONS", cartItemModel.getFreeCoupons());
                orderDetails.put("DELIVERY PRICE", cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice());
                orderDetails.put("CANCELLATION REQUESTED", false);

                firebaseFirestore.collection("ORDERS").document(order_id).collection("ORDER_ITEMS").document(cartItemModel.getProductID())
                        .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            String error = task.getException().getMessage();
                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {

                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("TOTAL ITEMS", cartItemModel.getTotalItems());
                orderDetails.put("TOTAL ITEM PRICE", cartItemModel.getTotalItemPrice());
                orderDetails.put("DELIVERY PRICE", cartItemModel.getDeliveryPrice());
                orderDetails.put("TOTAL AMOUNT", cartItemModel.getTotalAmount());
                orderDetails.put("SAVED AMOUNT", cartItemModel.getSavedAmount());
                orderDetails.put("PAYMENT STATUS", "Not Paid");
                orderDetails.put("ORDER STATUS", "Cancelled");

                firebaseFirestore.collection("ORDERS").document(order_id)
                        .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (paymentMethod.equals("PAYTM")) {
                                paytm();
                            } else {
                                cod();
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void paytm() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        loadingDialog.show();
        if (ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeliveryActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        final String M_id = "pzLlid71418101688109";
        final String customer_id = FirebaseAuth.getInstance().getUid();
        String url = "https://picklekart.000webhostapp.com/paytm/generateChecksum.php";
        final String callBackUrl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";

        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("CHECKSUMHASH")) {
                        String CHECKSUMHASH = jsonObject.getString("CHECKSUMHASH");

                        PaytmPGService paytmPGService = PaytmPGService.getStagingService();

                        HashMap<String, String> paramMap = new HashMap<String, String>();
                        paramMap.put("MID", M_id);
                        paramMap.put("ORDER_ID", order_id);
                        paramMap.put("CUST_ID", customer_id);
                        paramMap.put("CHANNEL_ID", "WAP");
                        paramMap.put("TXN_AMOUNT", totalCartAmount.getText().toString().substring(3, totalCartAmount.getText().length() - 2));
                        paramMap.put("WEBSITE", "WEBSTAGING");
                        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                        paramMap.put("CALLBACK_URL", callBackUrl);
                        paramMap.put("CHECKSUMHASH", CHECKSUMHASH);

                        PaytmOrder paytmOrder = new PaytmOrder(paramMap);

                        paytmPGService.initialize(paytmOrder, null);
                        paytmPGService.startPaymentTransaction(DeliveryActivity.this, true, true, new PaytmPaymentTransactionCallback() {
                            @Override
                            public void onTransactionResponse(Bundle inResponse) {
//                                        Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();

                                if (inResponse.getString("STATUS").equals("TXN_SUCCESS")) {

                                    Map<String, Object> updateStatus = new HashMap<>();
                                    updateStatus.put("PAYMENT STATUS", "Paid");
                                    updateStatus.put("ORDER STATUS", "Ordered");
                                    firebaseFirestore.collection("ORDERS").document(order_id)
                                            .update(updateStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Map<String, Object> userOrder = new HashMap<>();
                                                userOrder.put("order_id", order_id);
                                                userOrder.put("time", FieldValue.serverTimestamp());
                                                firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                                                        .collection("USER_ORDERS").document(order_id)
                                                        .set(userOrder).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            showConfirmationLayout();
                                                        } else {
                                                            Toast.makeText(DeliveryActivity.this, "Failed to update user list.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(DeliveryActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                }

                            }

                            @Override
                            public void networkNotAvailable() {
                                Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void clientAuthenticationFailed(String inErrorMessage) {
                                Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void someUIErrorOccurred(String inErrorMessage) {
                                Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage, Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                                Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage.toString(), Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onBackPressedCancelTransaction() {
                                Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.dismiss();
                Toast.makeText(DeliveryActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("MID", M_id);
                paramMap.put("ORDER_ID", order_id);
                paramMap.put("CUST_ID", customer_id);
                paramMap.put("CHANNEL_ID", "WAP");
                paramMap.put("TXN_AMOUNT", totalCartAmount.getText().toString().substring(3, totalCartAmount.getText().length() - 2));
                paramMap.put("WEBSITE", "WEBSTAGING");
                paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                paramMap.put("CALLBACK_URL", callBackUrl);

                return paramMap;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void cod() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        Intent otpIntent = new Intent(DeliveryActivity.this, OTPVerificationActivity.class);
        otpIntent.putExtra("mobileNo", mobileNo.substring(0, 10));
        otpIntent.putExtra("orderID", order_id);
        startActivity(otpIntent);
    }

}
