package com.example.picklekart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OTPVerificationActivity extends AppCompatActivity {

    private TextView phoneNo;
    private EditText otp;
    private Button verify;
    private String userNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

        phoneNo = findViewById(R.id.phone_number);
        otp = findViewById(R.id.otp);
        verify = findViewById(R.id.verify);
        userNo = getIntent().getStringExtra("mobileNo");
        phoneNo.setText("Verification code has been sent to +91 " + userNo);

        Random random = new Random();
        final int OTP_number = random.nextInt(999999 - 111111) + 111111;

        String SMS_API = "https://www.fast2sms.com/dev/bulk";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SMS_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                verify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (otp.getText().toString().equals(String.valueOf(OTP_number))) {
                            Map<String, Object> updateStatus = new HashMap<>();
                            updateStatus.put("ORDER STATUS", "Ordered");
                            final String orderID = getIntent().getStringExtra("orderID");
                            FirebaseFirestore.getInstance().collection("ORDERS").document(orderID)
                                    .update(updateStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> userOrder = new HashMap<>();
                                        userOrder.put("order_id", orderID);
                                        userOrder.put("time", FieldValue.serverTimestamp());
                                        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                                                .collection("USER_ORDERS").document(orderID)
                                                .set(userOrder).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    DeliveryActivity.codOrderConfirmed = true;
                                                    finish();
                                                } else {
                                                    Toast.makeText(OTPVerificationActivity.this, "Failed to update user list.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    } else {
                                        Toast.makeText(OTPVerificationActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(OTPVerificationActivity.this, "Incorrect OTP!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                finish();
                Toast.makeText(OTPVerificationActivity.this, "Failed to send the OTP verification sms.", Toast.LENGTH_SHORT).show();
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
                body.put("numbers", userNo);
                body.put("message", "16274");
                body.put("variables", "{#BB#}");
                body.put("variables_values", String.valueOf(OTP_number));

                return body;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue requestQueue = Volley.newRequestQueue(OTPVerificationActivity.this);
        requestQueue.add(stringRequest);
    }
}
