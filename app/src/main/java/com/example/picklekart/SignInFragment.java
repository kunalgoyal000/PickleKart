package com.example.picklekart;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.picklekart.RegisterActivity.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment {


    public SignInFragment() {
        // Required empty public constructor
    }

    private TextView dontHaveAnAccount;
    private FrameLayout parentFrameLayout;


    private EditText email;
    private EditText password;

    private TextView forgotPassword;

    private ImageButton closeBtn;
    private Button signInBtn;

    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore firebaseFirestore;

    private String emailPattern = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"

            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"

            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."

            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"

            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"

            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";


    public static boolean disableCloseBtn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        dontHaveAnAccount = view.findViewById(R.id.tv_dont_have_an_account);
        parentFrameLayout = getActivity().findViewById(R.id.register_framlayout);

        email = view.findViewById(R.id.sign_in_email);
        password = view.findViewById(R.id.sign_in_password);

        forgotPassword = view.findViewById(R.id.sign_in_forgot_password);

        closeBtn = view.findViewById(R.id.sign_in_close_button);
        signInBtn = view.findViewById(R.id.sign_in_button);

        progressBar = view.findViewById(R.id.sign_in_progress_bar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        if (disableCloseBtn) {
            closeBtn.setVisibility(View.GONE);
        } else {
            closeBtn.setVisibility(View.VISIBLE);
        }
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dontHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragment(new SignUpFragment());
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResetPasswordFragment = true;
                setFragment(new ResetPasswordFragment());
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainIntent();
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailAndPassword();
            }
        });
    }


    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slideout_from_left);
        fragmentTransaction.replace(parentFrameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty((email.getText()))) {
            if (!TextUtils.isEmpty(password.getText()) && password.length() >= 8) {
                signInBtn.setEnabled(true);
                signInBtn.setTextColor(Color.rgb(255, 255, 255));
            } else {
                signInBtn.setEnabled(false);
                signInBtn.setTextColor(Color.argb(50, 255, 255, 255));
            }
        } else {
            signInBtn.setEnabled(false);
            signInBtn.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }

    private void checkEmailAndPassword() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (email.getText().toString().matches(emailPattern)) {
            if (password.length() >= 8) {
                progressBar.setVisibility(View.VISIBLE);
                signInBtn.setEnabled(false);
                signInBtn.setTextColor(Color.argb(50, 255, 255, 255));

                firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseFirestore.getInstance().collection("USERS").document(firebaseAuth.getUid())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                DBqueries.fullName = task.getResult().getString("fullname");
                                                DBqueries.email = task.getResult().getString("email");
                                                DBqueries.profile = task.getResult().getString("profile");
                                                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                                                startActivity(mainIntent);
                                                getActivity().finish();

                                            } else {
                                                String error = task.getException().getMessage();
                                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    signInBtn.setEnabled(true);
                                    signInBtn.setTextColor(Color.rgb(255, 255, 255));
                                    String error = task.getException().getMessage();
                                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            } else {
                Toast.makeText(getActivity(), "Incorrect email or Password!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Incorrect email or Password!", Toast.LENGTH_SHORT).show();
        }

    }

    private void mainIntent() {
        if (disableCloseBtn) {
            disableCloseBtn = false;
        } else {
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(mainIntent);
        }
        getActivity().finish();

    }
}
