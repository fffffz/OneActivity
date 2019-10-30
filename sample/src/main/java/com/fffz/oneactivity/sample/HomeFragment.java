package com.fffz.oneactivity.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.fffz.oneactivity.AbstractFragment;

/**
 * Created by fffz on 2018/1/3.
 */
public class HomeFragment extends AbstractFragment {

    @Override
    public int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btn = (Button) view.findViewById(R.id.btn);
        btn.setText(getArguments().getString("key"));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(Home2Fragment.class);
            }
        });
    }
}
