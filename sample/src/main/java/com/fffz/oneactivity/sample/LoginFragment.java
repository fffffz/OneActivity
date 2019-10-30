package com.fffz.oneactivity.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.fffz.oneactivity.AbstractFragment;
import com.fffz.oneactivity.FragmentIntent;

/**
 * Created by fffz on 2018/1/4.
 */
public class LoginFragment extends AbstractFragment implements View.OnClickListener {

    private EditText mEtUsername;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_login).setOnClickListener(this);
        view.findViewById(R.id.btn_register).setOnClickListener(this);
        mEtUsername = view.findViewById(R.id.et_username);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login) {
            Bundle data = new Bundle();
            data.putString("username", mEtUsername.getText().toString());
            setResult(RESULT_OK, data);
            finish();
        } else if (id == R.id.btn_register) {
            FragmentIntent intent = new FragmentIntent(this, RegisterFragment.class);
            intent.setEnterAnim(R.anim.slide_in_right, R.anim.slide_out_left);
            intent.setExitAnim(R.anim.slide_out_right, R.anim.slide_in_left);
            startFragment(intent);
        }
    }

    @Override
    public int getLaunchMode() {
        return SINGLE_INSTANCE;
    }
}
