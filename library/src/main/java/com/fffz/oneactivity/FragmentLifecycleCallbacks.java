package com.fffz.oneactivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fffz on 2018/1/5.
 */
public class FragmentLifecycleCallbacks<T extends AbstractFragment> {

    protected void onCreateView(T fragment, LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
    }

    protected void onViewCreated(T fragment, View view, @Nullable Bundle savedInstanceState) {
    }

    protected void onNewIntent(T fragment, FragmentIntent intent) {
    }

    protected void onResumeView(T fragment) {

    }

    protected void onPauseView(T fragment) {
    }

    protected void onDestroyView(T fragment) {
    }

    protected void onPushEnter(T fragment) {

    }

    protected void onPushExit(T fragment) {

    }

    protected void onPopEnter(T fragment) {

    }

    protected void onPopExit(T fragment) {

    }

}