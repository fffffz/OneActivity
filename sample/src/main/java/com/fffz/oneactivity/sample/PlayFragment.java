package com.fffz.oneactivity.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.fffz.oneactivity.AbstractFragment;
import com.fffz.oneactivity.FragmentIntent;

/**
 * Created by fffz on 2017/12/28.
 */
public class PlayFragment extends AbstractFragment implements View.OnClickListener {

    private TextView mTextView;
    private Handler mHandler;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_play;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler();
        mTextView = (TextView) view.findViewById(R.id.tv);
        mTextView.setOnClickListener(this);
        view.findViewById(R.id.btn).setOnClickListener(this);
    }

    @Override
    public void onNewIntent(FragmentIntent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(getClass().getSimpleName(), "onNewIntent extras[{key=" + extras.getString("key")
                    + "}]");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("text", mTextView.getText());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        mTextView.setText(savedInstanceState.getCharSequence("text"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv:
                mTextView.setText("clicked");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentIntent intent = new FragmentIntent(PlayFragment.this,
                                PlayFragment.class);
                        Bundle extras = new Bundle();
                        extras.putString("key", "value");
                        intent.putExtras(extras);
                        intent.setEnterAnim(R.anim.slide_in_bottom, 0);
                        intent.setExitAnim(R.anim.slide_out_bottom, 0);
                        startFragment(intent);
                    }
                }, 1000);
                break;
            case R.id.btn:
                Bundle data = new Bundle();
                data.putString("key", "value");
                setResult(RESULT_OK, data);
                finish();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }

}