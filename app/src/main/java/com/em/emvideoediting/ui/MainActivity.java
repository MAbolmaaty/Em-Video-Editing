package com.em.emvideoediting.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.em.emvideoediting.R;
import com.em.emvideoediting.adapters.MainPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomNavigationView mBottomNavigationView;
    private ViewPager2 mViewPager;
    private MainPagerAdapter mMainPagerAdapter;
    private TextView mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Don't Allow Night Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Hide App Bar
        getSupportActionBar().hide();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Read External Storage is not granted");
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissions,
            100);
        }
        mBottomNavigationView = findViewById(R.id.mainBottomNav);
        mViewPager = findViewById(R.id.mainViewPager);
        mToolbarTitle = findViewById(R.id.toolbarTitle);
        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getLifecycle());
        mViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        mViewPager.setAdapter(mMainPagerAdapter);
        mViewPager.setCurrentItem(0, false);
        mViewPager.setOffscreenPageLimit(4);
        mBottomNavigationView.setOnItemSelectedListener(bottomNavItemSelected());
        mViewPager.registerOnPageChangeCallback(viewPagerChange());
        mViewPager.setPageTransformer(new DepthPageTransformer());
    }

    private void loadFragment(FragmentManager fragmentManager,
                                    Fragment fragment,
                                    boolean addToBackStack) {
        if (addToBackStack) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    private NavigationBarView.OnItemSelectedListener bottomNavItemSelected(){
        NavigationBarView.OnItemSelectedListener itemSelected =
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.nav_video_trim:
                                navToTrimPage();
                                return true;
                            case R.id.nav_video_merge:
                                navToMergePage();
                                return true;
                            case R.id.nav_video_water_mark:
                                navToWaterMarkPage();
                                return true;
                            case R.id.nav_about_app:
                                navToAboutAppPage();
                                return true;
                        }
                        return false;
                    }
                };
        return itemSelected;
    }

    private ViewPager2.OnPageChangeCallback viewPagerChange(){
        ViewPager2.OnPageChangeCallback pageChangeCallback =
                new ViewPager2.OnPageChangeCallback(){
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        switch (position){
                            case 0:
                                mBottomNavigationView.setSelectedItemId(R.id.nav_video_trim);
                                navToTrimPage();
                                break;
                            case 1:
                                mBottomNavigationView.setSelectedItemId(R.id.nav_video_merge);
                                navToMergePage();
                                break;
                            case 2:
                                mBottomNavigationView.setSelectedItemId(R.id.nav_video_water_mark);
                                navToWaterMarkPage();
                                break;
                            case 3:
                                mBottomNavigationView.setSelectedItemId(R.id.nav_about_app);
                                navToAboutAppPage();
                                break;
                        }
                    }
                };
        return pageChangeCallback;
    }

    private void navToTrimPage(){
        mToolbarTitle.setText(R.string.trim);
        mViewPager.setCurrentItem(0);
    }

    private void navToMergePage(){
        mToolbarTitle.setText(R.string.merge);
        mViewPager.setCurrentItem(1);
    }

    private void navToWaterMarkPage(){
        mToolbarTitle.setText(R.string.water_mark);
        mViewPager.setCurrentItem(2);
    }

    private void navToAboutAppPage(){
        mToolbarTitle.setText(R.string.about_app);
        mViewPager.setCurrentItem(3);
    }

    public class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setTranslationZ(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Move it behind the left page
                view.setTranslationZ(-1f);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}