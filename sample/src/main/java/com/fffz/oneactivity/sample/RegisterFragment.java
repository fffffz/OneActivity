package com.fffz.oneactivity.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.fffz.oneactivity.AbstractFragment;

/**
 * Created by fffz on 2018/1/8.
 */
public class RegisterFragment extends AbstractFragment {
    @Override
    public int getLayoutId() {
        return R.layout.fragment_register;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}