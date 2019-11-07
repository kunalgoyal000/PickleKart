package com.example.picklekart;


import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyRewardsFragment extends Fragment {


    private Dialog loadingDialog;

    private RecyclerView rewardsRecyclerView;
    private TextView no_rewards;
    public static RewardsAdapter rewardsAdapter;

    public MyRewardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_rewards, container, false);

        /////////// Loading Dialog

        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));

        loadingDialog.show();

        //////////// Loading Dialog
        rewardsRecyclerView = view.findViewById(R.id.my_rewards_recyclerview);
        no_rewards = view.findViewById(R.id.tv_no_rewards);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rewardsRecyclerView.setLayoutManager(linearLayoutManager);
        rewardsAdapter = new RewardsAdapter(DBqueries.rewardModelList, false);
        rewardsRecyclerView.setAdapter(rewardsAdapter);
        rewardsRecyclerView.setVisibility(View.VISIBLE);


        if (DBqueries.rewardModelList.size() == 0) {
            DBqueries.loadRewards(getContext(), loadingDialog, true);
        } else {
            loadingDialog.dismiss();
        }

        rewardsAdapter.notifyDataSetChanged();

        return view;
    }

}
