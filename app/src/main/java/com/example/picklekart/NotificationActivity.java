package com.example.picklekart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView noNotifications;
    public static NotificationAdapter adapter;
    private boolean runQuery = false;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Notifications");

        recyclerView = findViewById(R.id.recycler_view);
        noNotifications = findViewById(R.id.tv_no_notifications);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new NotificationAdapter(DBqueries.notificationModelList);
        recyclerView.setAdapter(adapter);

        if (DBqueries.notificationModelList.size() == 0) {
            noNotifications.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noNotifications.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            Map<String, Object> readMap = new HashMap<>();
            for (int x = 0; x < DBqueries.notificationModelList.size(); x++) {
                if (!DBqueries.notificationModelList.get(x).isReaded()) {
                    runQuery = true;
                }
                readMap.put("Readed_" + x, true);
            }
            if (runQuery) {
                FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_NOTIFICATIONS")
                        .update(readMap);

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int x = 0; x < DBqueries.notificationModelList.size(); x++) {
            DBqueries.notificationModelList.get(x).setReaded(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);


    }


}
