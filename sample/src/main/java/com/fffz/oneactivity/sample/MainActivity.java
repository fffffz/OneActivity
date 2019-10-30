package com.fffz.oneactivity.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.fffz.oneactivity.AbstractFragment;
import com.fffz.oneactivity.FragmentLifecycleCallbacks;
import com.fffz.oneactivity.OneActivity;


public class MainActivity extends OneActivity implements View.OnClickListener {

    private View mNavBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.tab_home).setOnClickListener(this);
        findViewById(R.id.tab_library).setOnClickListener(this);
        findViewById(R.id.tab_feed).setOnClickListener(this);
        findViewById(R.id.tab_profile).setOnClickListener(this);
        Bundle args = new Bundle();
        args.putString("key", "Home2");
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(args);
        addTask(0, homeFragment);
        showTask(0);
        mNavBar = findViewById(R.id.ll_nav_bar);
        registerFragmentLifecycleCallbacks(new FragmentLifecycleCallbacks() {
            @Override
            protected void onResumeView(AbstractFragment fragment) {
                if (fragment instanceof HomeFragment || fragment instanceof LibraryFragment
                        || fragment instanceof FeedFragment || fragment instanceof ProfileFragment) {
                    mNavBar.setVisibility(View.VISIBLE);
                } else {
                    mNavBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getContainerId() {
        return R.id.container;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tab_home: {
                if (!isTaskAdded(0)) {
                    addTask(0, new HomeFragment());
                }
                showTask(0);
            }
            break;
            case R.id.tab_library: {
                if (!isTaskAdded(1)) {
                    addTask(1, new LibraryFragment());
                }
                showTask(1);
            }
            break;
            case R.id.tab_feed: {
                if (!isTaskAdded(2)) {
                    addTask(2, new FeedFragment());
                }
                showTask(2);
            }
            break;
            case R.id.tab_profile: {
                if (!isTaskAdded(3)) {
                    addTask(3, new ProfileFragment());
                }
                showTask(3);
            }
            break;
        }
    }

}