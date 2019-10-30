package com.fffz.oneactivity.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.fffz.oneactivity.AbstractFragment;
import com.fffz.oneactivity.FragmentIntent;

/**
 * Created by fffz on 2018/1/3.
 */
public class ProfileFragment extends AbstractFragment {

    @Override
    public int getLayoutId() {
        return R.layout.fragment_profile;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentIntent intent = new FragmentIntent(ProfileFragment.this, LoginFragment.class);
                intent.setEnterAnim(R.anim.slide_in_bottom, 0);
                intent.setExitAnim(R.anim.slide_out_bottom, 0);
                intent.setLaunchAction(FragmentIntent.ACTION_CLEAR_TOP);
                startFragmentForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(getContext(), data.getString("username"), Toast.LENGTH_SHORT).show();
        }
    }

}