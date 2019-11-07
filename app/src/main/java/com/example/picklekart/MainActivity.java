package com.example.picklekart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.picklekart.DBqueries.cartList;
import static com.example.picklekart.DBqueries.firebaseFirestore;
import static com.example.picklekart.RegisterActivity.setSignUpFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FrameLayout frameLayout;

    private static final int HOME_FRAGMENT = 0;
    private static final int CART_FRAGMENT = 1;
    private static final int ORDERS_FRAGMENT = 2;
    private static final int WISHLIST_FRAGMENT = 3;
    private static final int REWARDS_FRAGMENT = 4;
    private static final int ACCOUNT_FRAGMENT = 5;
    public static Boolean showCart = false;

    public static Activity mainActivity;
    public static boolean resetMainActivity = false;

    private int currentFragment = -1;

    private NavigationView navigationView;
    public static DrawerLayout drawer;

    private ImageView actionBarLogo;

    private Window window;
    public static Toolbar toolbar;

    private Dialog signInDialog;

    private FirebaseUser currentUser;

    public static MenuItem cartItem, notifyItem;

    private TextView badgeCount;
    private int scrollFlags;
    private AppBarLayout.LayoutParams params;

    private CircleImageView profileView;
    private TextView fullName, email;
    // private ImageView addProfileIcon;
    private MenuItem searchItem;


    public static List<WishlistModel> list;
    public static List<String> ids;
    private Dialog loadingDialog;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /////////// Loading Dialog


        loadingDialog = new Dialog(MainActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));

        //////////// Loading Dialog


        frameLayout = findViewById(R.id.main_framelayout);

        toolbar = findViewById(R.id.toolbar);
        actionBarLogo = findViewById(R.id.actionbar_logo);
        setSupportActionBar(toolbar);

        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


        params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        scrollFlags = params.getScrollFlags();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        profileView = navigationView.getHeaderView(0).findViewById(R.id.main_profile_image);
        fullName = navigationView.getHeaderView(0).findViewById(R.id.main_fullname);
        email = navigationView.getHeaderView(0).findViewById(R.id.main_email);
        //    addProfileIcon = navigationView.getHeaderView(0).findViewById(R.id.add_profile_icon);


        list = new ArrayList<>();
        ids = new ArrayList<>();

        if (showCart) {
            mainActivity = this;
            drawer.setDrawerLockMode(1);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            gotoFragment("My Cart", new MyCartFragment(), -2);

        } else {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            setFragment(new HomeFragment(), HOME_FRAGMENT);
        }

        signInDialog = new Dialog(MainActivity.this);
        signInDialog.setContentView(R.layout.sign_in_dialog);

        signInDialog.setCancelable(true);

        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button dialogSignInBtn = signInDialog.findViewById(R.id.sign_in_btn);
        Button dialogSignUpBtn = signInDialog.findViewById(R.id.sign_up_btn);

        final Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);

        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInFragment.disableCloseBtn = true;
                SignUpFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = false;
                startActivity(registerIntent);
            }
        });

        dialogSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInFragment.disableCloseBtn = true;
                SignUpFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = true;
                startActivity(registerIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setEnabled(false);
        } else {

            if (DBqueries.email == null) {
                FirebaseFirestore.getInstance().collection("USERS").document(currentUser.getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DBqueries.fullName = task.getResult().getString("fullname");
                            DBqueries.email = task.getResult().getString("email");
                            DBqueries.profile = task.getResult().getString("profile");

                            fullName.setText(DBqueries.fullName);
                            email.setText(DBqueries.email);
                            if (DBqueries.profile.equals("")) {
                                profileView.setImageResource(R.drawable.placeholder_image_icon);
                            } else {
                                Glide.with(MainActivity.this).load(DBqueries.profile).apply(new RequestOptions().placeholder(R.drawable.placeholder_image_icon)).into(profileView);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                fullName.setText(DBqueries.fullName);
                email.setText(DBqueries.email);
                if (DBqueries.profile.equals("")) {
                    profileView.setImageResource(R.drawable.placeholder_image_icon);
                } else {
                    Glide.with(MainActivity.this).load(DBqueries.profile).apply(new RequestOptions().placeholder(R.drawable.placeholder_image_icon)).into(profileView);
                }
            }
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 1).setEnabled(true);


        }
        if (resetMainActivity) {

            resetMainActivity = false;
            actionBarLogo.setVisibility(View.VISIBLE);
            setFragment(new HomeFragment(), HOME_FRAGMENT);
            navigationView.getMenu().getItem(0).setChecked(true);

        }
        invalidateOptionsMenu();

    }

    @Override
    protected void onPause() {
        super.onPause();
        DBqueries.checkNotifications(true, null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentFragment == HOME_FRAGMENT) {
                currentFragment = -1;
                super.onBackPressed();
            } else {
                if (showCart) {
                    mainActivity = null;
                    showCart = false;
                    finish();
                } else {

                    actionBarLogo.setVisibility(View.VISIBLE);
                    invalidateOptionsMenu();
                    setFragment(new HomeFragment(), HOME_FRAGMENT);
                    navigationView.getMenu().getItem(0).setChecked(true);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (currentFragment == HOME_FRAGMENT) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getMenuInflater().inflate(R.menu.main, menu);

            cartItem = menu.findItem(R.id.main_cart_icon);

            cartItem.setActionView(R.layout.badge_layout);
            ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
            badgeIcon.setImageResource(R.drawable.action_cart_icon);
            badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);
            if (currentUser != null) {

                if (cartList.size() == 0) {
                    badgeCount.setVisibility(View.INVISIBLE);
                    DBqueries.loadCartList(MainActivity.this, new Dialog(MainActivity.this), false, badgeCount, new TextView(MainActivity.this));
                } else {
                    badgeCount.setVisibility(View.VISIBLE);
                    if (cartList.size() < 99) {
                        badgeCount.setText(String.valueOf(cartList.size()));
                    } else {
                        badgeCount.setText("99+");
                    }
                }
            }

            cartItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentUser == null) {
                        signInDialog.show();
                    } else {
                        gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);
                    }
                }
            });


            notifyItem = menu.findItem(R.id.main_notification_icon);

            notifyItem.setActionView(R.layout.badge_layout);
            ImageView notifyIcon = notifyItem.getActionView().findViewById(R.id.badge_icon);
            notifyIcon.setImageResource(R.drawable.notification_icon);
            TextView notifyCount = notifyItem.getActionView().findViewById(R.id.badge_count);

            if (currentUser != null) {
                DBqueries.checkNotifications(false, notifyCount);

            }

            notifyItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
                    startActivity(notificationIntent);

                }
            });

            searchItem = menu.findItem(R.id.main_search_icon);

            SearchView mSearchView = (SearchView) searchItem.getActionView();
            mSearchView.setQueryHint("Search for Product");


            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {
                    loadingDialog.show();
                    list.clear();
                    ids.clear();


                    final String[] tags = query.toLowerCase().split(" ");
                    for (final String tag : tags) {
                        tag.trim();
                        FirebaseFirestore.getInstance().collection("PRODUCTS").whereArrayContains("tags", tag).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                        WishlistModel model = new WishlistModel(documentSnapshot.getId(), documentSnapshot.get("product_image_1").toString()
                                                , documentSnapshot.get("product_title").toString()
                                                , (long) documentSnapshot.get("free_coupons")
                                                , documentSnapshot.get("average_rating").toString()
                                                , (long) documentSnapshot.get("total_ratings")
                                                , documentSnapshot.get("product_price").toString()
                                                , documentSnapshot.get("cutted_price").toString()
                                                , (boolean) documentSnapshot.get("COD")
                                                , true);

                                        model.setTags((ArrayList<String>) documentSnapshot.get("tags"));
                                        if (!ids.contains(model.getProductId())) {
                                            list.add(model);
                                            ids.add(model.getProductId());
                                        }
                                    }

                                    if (tag.equals(tags[tags.length - 1])) {
                                        if (list.size() == 0) {
                                            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                                            searchIntent.putExtra("textview", "VISIBLE");
                                            startActivity(searchIntent);
                                            loadingDialog.dismiss();
                                        } else {
                                            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                                            searchIntent.putExtra("textview", "GONE");
                                            searchIntent.putExtra("query", query);
                                            startActivity(searchIntent);
                                            loadingDialog.dismiss();

                                        }
                                    }

                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
        return true;
    }

    /////search adapter


    /////search adapter


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.main_search_icon) {
//
//
//            Intent searchIntent=new Intent(this,SearchActivity.class);
//            startActivity(searchIntent);
//            return true;
//        } else
        if (id == R.id.main_notification_icon) {
            Intent notificationIntent = new Intent(this, NotificationActivity.class);
            startActivity(notificationIntent);
            return true;
        } else if (id == R.id.main_cart_icon) {
            if (currentUser == null) {
                signInDialog.show();
            } else {
                gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);
            }
            return true;
        } else if (id == android.R.id.home) {
            if (showCart) {
                mainActivity = null;
                showCart = false;
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void gotoFragment(String title, Fragment fragment, int fragmentNum) {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(title);
        actionBarLogo.setVisibility(View.GONE);
        invalidateOptionsMenu();
        setFragment(fragment, fragmentNum);
        if (fragmentNum == CART_FRAGMENT || showCart) {
            navigationView.getMenu().getItem(3).setChecked(true);
            params.setScrollFlags(0);
        } else {
            params.setScrollFlags(scrollFlags);
        }
    }

    MenuItem menuItem;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        menuItem = item;

        if (currentUser != null) {
            drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    int id = menuItem.getItemId();

                    if (id == R.id.nav_pickle_kart) {
                        actionBarLogo.setVisibility(View.VISIBLE);
                        invalidateOptionsMenu();
                        setFragment(new HomeFragment(), HOME_FRAGMENT);
                    } else if (id == R.id.nav_my_orders) {
                        gotoFragment("My Orders", new MyOrdersFragment(), ORDERS_FRAGMENT);

                    } else if (id == R.id.nav_my_rewards) {
                        gotoFragment("My Rewards", new MyRewardsFragment(), REWARDS_FRAGMENT);

                    } else if (id == R.id.nav_my_cart) {
                        gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);

                    } else if (id == R.id.nav_my_wishlist) {
                        gotoFragment("My Wishlist", new WishlistFragment(), WISHLIST_FRAGMENT);

                    } else if (id == R.id.nav_my_account) {

                        gotoFragment("My Account", new MyAccountFragment(), ACCOUNT_FRAGMENT);

                    } else if (id == R.id.nav_sign_out) {
                        FirebaseAuth.getInstance().signOut();
                        DBqueries.clearData();
                        Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivity(registerIntent);
                        finish();
                    }
                    drawer.removeDrawerListener(this);
                }
            });

            return true;
        } else {
            signInDialog.show();
            return false;
        }

    }

    private void setFragment(Fragment fragment, int fragmentNo) {
        if (fragmentNo != currentFragment) {
            if (fragmentNo == REWARDS_FRAGMENT) {
                window.setStatusBarColor(Color.parseColor("#5b04b1"));
                toolbar.setBackgroundColor(Color.parseColor("#5b04b1"));
            } else {
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            currentFragment = fragmentNo;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            fragmentTransaction.replace(frameLayout.getId(), fragment);
            fragmentTransaction.commit();
        }
    }
}
