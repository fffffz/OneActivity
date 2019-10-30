package com.fffz.oneactivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by fffz on 2017/12/26.
 */
public abstract class AbstractFragment extends Fragment {

    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_OK = Activity.RESULT_OK;

    FragmentRecord mFragmentRecord;
    int mResultCode = RESULT_CANCELED;
    Bundle mResultData = null;
    Object[] mResultData2;
    protected OneActivity mActivity;
    protected View mRootView;
    boolean mCreated;
    boolean mResumed;
    boolean mDestroyed;
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private ArrayList<LifecycleCallbacks> mLifecycleCallbacks = new ArrayList<>();

    public static final int STANDARD = 0x000;
    public static final int SINGLE_TOP = 0x001;
    public static final int SINGLE_TASK = 0x002;
    public static final int SINGLE_INSTANCE = 0x003;

    public boolean onBackPressed() {
        return false;
    }

    @IntDef(flag = true,
            value = {STANDARD, SINGLE_TOP, SINGLE_TASK, SINGLE_INSTANCE})
    @Retention(RetentionPolicy.SOURCE)
    @interface LaunchMode {
    }

    public AbstractFragment() {
    }

    @CallSuper
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mActivity == null) {
            mActivity = (OneActivity) getActivity();
        }
        mActivity.dispatchFragmentOnCreateView(this, inflater, container, savedInstanceState);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onCreateView(inflater, container, savedInstanceState);
        }
        //fragment is restoring, because activity has been recycled before
        if (savedInstanceState != null) {
            mFragmentRecord = mActivity.findFragmentRecord(getTag());
            if (mFragmentRecord != null) {
                mFragmentRecord.setFragment(this);
            }
        }
        int layoutId = getLayoutId();
        if (layoutId == -1) {
            return null;
        }
        mRootView = inflater.inflate(layoutId, container, false);
        mRootView.setClickable(true);
        return mRootView;
    }

    protected int getLayoutId() {
        return -1;
    }

    public @LaunchMode
    int getLaunchMode() {
        return STANDARD;
    }

    /**
     * @return pushEnterAnim pushExitAnim popEnterAnim popExitAnim
     */
    public int[] getAnimations() {
        return new int[]{R.anim.slide_in_right, 0, 0, R.anim.slide_out_right};
    }

    public int getTaskTag() {
        return mFragmentRecord.mTaskRecord.mTaskTag;
    }

    public final void startFragment(FragmentIntent intent) {
        mActivity.startFragment(intent);
    }

    public final void startFragment(Class<? extends AbstractFragment> toFragmentClass) {
        mActivity.startFragment(this, toFragmentClass);
    }

    public final void startFragment(Class<? extends AbstractFragment> toFragmentClass,
                                    @FragmentIntent.LaunchAction int launchMode) {
        mActivity.startFragment(this, toFragmentClass, launchMode);
    }

    public final void startFragmentForResult(FragmentIntent intent, int requestCode) {
        mActivity.startFragmentForResult(intent, requestCode);
    }

    public final void startFragmentForResult(Class<? extends AbstractFragment> toFragmentClass,
                                             int requestCode) {
        mActivity.startFragmentForResult(this, toFragmentClass, requestCode);
    }

    public final void startFragmentForResult(Class<? extends AbstractFragment> toFragmentClass,
                                             FragmentResultCallback resultCallback) {
        mActivity.startFragmentForResult(this, toFragmentClass, resultCallback);
    }

    public final void startFragmentForResult(Class<? extends AbstractFragment> toFragmentClass,
                                             @FragmentIntent.LaunchAction int launchMode,
                                             int requestCode) {
        mActivity.startFragmentForResult(this, toFragmentClass, launchMode, requestCode);
    }

    public final void startFragmentForResult(Class<? extends AbstractFragment> toFragmentClass,
                                             @FragmentIntent.LaunchAction int launchMode,
                                             FragmentResultCallback resultCallback) {
        mActivity.startFragmentForResult(this, toFragmentClass, launchMode, resultCallback);
    }

    final void dispatchOnResumeView() {
        if (!mCreated || mResumed) {
            return;
        }
        onResumeView();
    }

    final void dispatchOnPauseView() {
        if (!mCreated || !mResumed) {
            return;
        }
        onPauseView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @CallSuper
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @CallSuper
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mCreated = true;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dispatchOnResumeView();
                onPushEnter();
            }
        });
        mActivity.dispatchFragmentOnViewCreated(this, view, savedInstanceState);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onViewCreated(view, savedInstanceState);
        }
    }

    @CallSuper
    public void onPauseView() {
        mResumed = false;
        mActivity.dispatchFragmentOnPauseView(this);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onPauseView();
        }
    }

    @CallSuper
    public void onResumeView() {
        mResumed = true;
        mActivity.dispatchFragmentOnResumeView(this);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onResumeView();
        }
    }

    @CallSuper
    public void onNewIntent(FragmentIntent intent) {
        mActivity.dispatchFragmentOnNewIntent(this, intent);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onNewIntent(intent);
        }
    }

    @CallSuper
    @Override
    public void onDestroyView() {
        if (mResumed) {
            onPauseView();
        }
        mDestroyed = true;
        mActivity.dispatchFragmentOnDestroyView(this);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onDestroyView();
        }
        super.onDestroyView();
    }

    protected void onPushEnter() {
        mActivity.dispatchFragmentOnPushEnter(this);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onPushEnter();
        }
    }

    protected void onPushExit() {
        mActivity.dispatchFragmentOnPushExit(this);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onPushExit();
        }
    }

    protected void onPopEnter() {
        mActivity.dispatchFragmentOnPopEnter(this);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onPopEnter();
        }
    }

    protected void onPopExit() {
        mActivity.dispatchFragmentOnPopExit(this);
        for (LifecycleCallbacks callbacks : mLifecycleCallbacks) {
            callbacks.onPopExit();
        }
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (!mActivity.isTopFragment(this)) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mActivity.isTopFragment(AbstractFragment.this)) {
                    return;
                }
                dispatchOnResumeView();
            }
        });
    }

    @Override
    public final void onPause() {
        super.onPause();
        if (!mActivity.isTopFragment(this)) {
            return;
        }
        dispatchOnPauseView();
    }

    @Override
    public final void onStop() {
        super.onStop();
        if (!mActivity.isTopFragment(this)) {
            return;
        }
        dispatchOnPauseView();
    }

    public void finish() {
        mActivity.finishFragment(this);
    }

    public void finish(int enterAnim, int exitAnim) {
        mActivity.finishFragment(this, enterAnim, exitAnim);
    }

    public void show() {
        mActivity.showFragment(this);
    }

    public void hide() {
        mActivity.hideFragment(this);
    }

    public void setHidden(boolean hidden) {
        mActivity.setFragmentHidden(this, hidden);
    }

    public void startAnimation(int anim) {
        if (mRootView == null || anim <= 0) {
            return;
        }
        mRootView.startAnimation(AnimationUtils.loadAnimation(mActivity, anim));
    }

    public final void setResult(int resultCode) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData = null;
            mResultData2 = null;
        }
    }

    public FragmentIntent getIntent() {
        if (mFragmentRecord == null) {
            return null;
        }
        return mFragmentRecord.mIntent;
    }

    public OneActivity getOneActivity() {
        return mActivity;
    }

    public final void setResult(int resultCode, Object... result) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData2 = result;
        }
    }

    public final void setResult(int resultCode, Bundle data) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData = data;
            mResultData2 = new Object[]{data};
        }
    }

    public int getResultCode() {
        return mResultCode;
    }

    public Bundle getResultData() {
        return mResultData;
    }

    protected void onFragmentResult(int requestCode, int resultCode, Bundle data) {
    }

    public void registerLifecycleCallbacks(LifecycleCallbacks callback) {
        synchronized (mLifecycleCallbacks) {
            mLifecycleCallbacks.add(callback);
        }
    }

    public void unregisterLifecycleCallbacks(LifecycleCallbacks callback) {
        synchronized (mLifecycleCallbacks) {
            mLifecycleCallbacks.remove(callback);
        }
    }

    public static class LifecycleCallbacks {

        protected void onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                    @Nullable Bundle savedInstanceState) {
        }

        protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        }

        protected void onNewIntent(FragmentIntent intent) {
        }

        protected void onResumeView() {

        }

        protected void onPauseView() {
        }

        protected void onDestroyView() {
        }

        protected void onPushEnter() {

        }

        protected void onPushExit() {

        }

        protected void onPopEnter() {

        }

        protected void onPopExit() {

        }

    }

}