package com.example.picklekart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.picklekart.DeliveryActivity.SELECT_ADDRESS;

public class MyAddressesActivity extends AppCompatActivity {

    private LinearLayout addNewAddressBtn;
    private RecyclerView myAddressesRecyclerView;
    private static AddressesAdapter addressesAdapter;
    private Button deliverHereBtn;
    private TextView addressesSaved;
    private int previousAddress;
    private Dialog loadingDialog;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_addresses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("My Addresses");

        /////////// Loading Dialog

        previousAddress = DBqueries.selectedAddress;

        myAddressesRecyclerView = findViewById(R.id.addresses_reyclerview);
        deliverHereBtn = findViewById(R.id.deliver_here_btn);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.slider_background));
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                addressesSaved.setText(String.valueOf(DBqueries.addressesModelList.size()) + " saved addresses");

            }
        });

        //////////// Loading Dialog

        addNewAddressBtn = findViewById(R.id.add_new_address_btn);
        addressesSaved = findViewById(R.id.address_saved);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        myAddressesRecyclerView.setLayoutManager(linearLayoutManager);


        mode = getIntent().getIntExtra("MODE", -1);
        if (mode == SELECT_ADDRESS) {
            deliverHereBtn.setVisibility(View.VISIBLE);
        } else {
            deliverHereBtn.setVisibility(View.GONE);
        }

        deliverHereBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DBqueries.selectedAddress != previousAddress) {
                    final int previousAddressIndex = previousAddress;
                    loadingDialog.show();

                    Map<String, Object> updateSelection = new HashMap<>();
                    updateSelection.put("selected_" + String.valueOf(previousAddress + 1), false);
                    updateSelection.put("selected_" + String.valueOf(DBqueries.selectedAddress + 1), true);

                    previousAddress = DBqueries.selectedAddress;
                    FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_ADDRESSES")
                            .update(updateSelection).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                finish();
                            } else {
                                previousAddress = previousAddressIndex;
                                String error = task.getException().getMessage();
                                Toast.makeText(MyAddressesActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            loadingDialog.dismiss();
                        }
                    });
                } else {
                    finish();
                }
            }
        });

        addressesAdapter = new AddressesAdapter(DBqueries.addressesModelList, mode, loadingDialog);
        myAddressesRecyclerView.setAdapter(addressesAdapter);
        ((SimpleItemAnimator) myAddressesRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        addressesAdapter.notifyDataSetChanged();

        addNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addAddressIntent = new Intent(MyAddressesActivity.this, AddAddressActivity.class);

                if (mode != SELECT_ADDRESS) {
                    addAddressIntent.putExtra("INTENT", "manage");
                } else {
                    addAddressIntent.putExtra("INTENT", "null");

                }
                startActivity(addAddressIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        addressesSaved.setText(String.valueOf(DBqueries.addressesModelList.size()) + " saved addresses");
        super.onStart();
    }

    public static void refreshItem(int deselect, int select) {
        addressesAdapter.notifyItemChanged(deselect);
        addressesAdapter.notifyItemChanged(select);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {

            if (mode == SELECT_ADDRESS) {
                if (DBqueries.selectedAddress != previousAddress) {
                    DBqueries.addressesModelList.get(DBqueries.selectedAddress).setSelected(false);
                    DBqueries.addressesModelList.get(previousAddress).setSelected(true);
                    DBqueries.selectedAddress = previousAddress;
                }
            } else {
                DBqueries.selectedAddress = 0;

            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mode == SELECT_ADDRESS) {
            if (DBqueries.selectedAddress != previousAddress) {
                DBqueries.addressesModelList.get(DBqueries.selectedAddress).setSelected(false);
                DBqueries.addressesModelList.get(previousAddress).setSelected(true);
                DBqueries.selectedAddress = previousAddress;

            }
        }
        super.onBackPressed();
    }
}
