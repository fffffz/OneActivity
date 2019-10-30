package com.fffz.oneactivity.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.fffz.oneactivity.AbstractFragment;

public class Home2Fragment extends AbstractFragment {

    @Override
    public int getLayoutId() {
        return R.layout.fragment_home_2;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(LoginFragment.class);
            }
        });
    }
}
