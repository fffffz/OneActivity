package com.fffz.oneactivity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AnimRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fffz on 2017/12/21.
 */
public class FragmentIntent implements Parcelable {

    protected FragmentIntent(Parcel in) {
        mLaunchAction = in.readInt();
        mContainerId = in.readInt();
        mShouldHideTopFragment = in.readInt();
        mExtras = in.readBundle();
        mPushEnterAnim = in.readInt();
        mPushExitAnim = in.readInt();
        mPopExitAnim = in.readInt();
        mPopEnterAnim = in.readInt();
    }

    public static final Creator<FragmentIntent> CREATOR = new Creator<FragmentIntent>() {
        @Override
        public FragmentIntent createFromParcel(Parcel in) {
            return new FragmentIntent(in);
        }

        @Override
        public FragmentIntent[] newArray(int size) {
            return new FragmentIntent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mLaunchAction);
        dest.writeInt(mContainerId);
        dest.writeInt(mShouldHideTopFragment);
        dest.writeBundle(mExtras);
        dest.writeInt(mPushEnterAnim);
        dest.writeInt(mPushExitAnim);
        dest.writeInt(mPopExitAnim);
        dest.writeInt(mPopEnterAnim);
    }

    @IntDef(flag = true,
            value = {ACTION_NONE, ACTION_CLEAR_TOP, ACTION_CLEAR_TASK})
    @Retention(RetentionPolicy.SOURCE)
    @interface LaunchAction {
    }

    public static final int ACTION_NONE = 0x000;
    public static final int ACTION_CLEAR_TOP = 0x002;
    public static final int ACTION_CLEAR_TASK = 0x003;
    public static final int ANIM_NOT_SET = Integer.MIN_VALUE;

    private AbstractFragment mFrom;
    private Class<? extends AbstractFragment> mTo;
    private int mLaunchAction;
    private int mContainerId;
    int mShouldHideTopFragment = 0;
    private Bundle mExtras;
    private int mPushEnterAnim = ANIM_NOT_SET;
    private int mPushExitAnim = ANIM_NOT_SET;
    private int mPopExitAnim = ANIM_NOT_SET;
    private int mPopEnterAnim = ANIM_NOT_SET;

    public FragmentIntent(@NonNull AbstractFragment from, @NonNull Class<? extends AbstractFragment> to) {
        mFrom = from;
        mTo = to;
    }

    public void setLaunchAction(@LaunchAction int launchAction) {
        mLaunchAction = launchAction;
    }

    public void setContainerId(int containerId) {
        mContainerId = containerId;
    }

    public void setShouldHideTopFragment(boolean shouldHideTopFragment) {
        mShouldHideTopFragment = shouldHideTopFragment ? 1 : 0;
    }

    public void setEnterAnim(@AnimRes int enterAnim, @AnimRes int topFragmentAnim) {
        mPushEnterAnim = enterAnim;
        mPushExitAnim = topFragmentAnim;
    }

    /**
     * 回退 (finish) 的时候生效，若是因为 startFragment() 被压入栈，则生效的是目标 Fragment 的动画配置
     */
    public void setExitAnim(@AnimRes int exitAnim, @AnimRes int topFragmentAnim) {
        mPopExitAnim = exitAnim;
        mPopEnterAnim = topFragmentAnim;
    }

    public  AbstractFragment getFrom() {
        return mFrom;
    }

    public Class<? extends AbstractFragment> getTo() {
        return mTo;
    }

    public int getLaunchAction() {
        return mLaunchAction;
    }

    public int getContainerId() {
        return mContainerId;
    }

    public boolean shouldHideTopFragment() {
        return mShouldHideTopFragment == 0;
    }

    public int getPushEnterAnim() {
        return mPushEnterAnim;
    }

    public int getPushExitAnim() {
        return mPushExitAnim;
    }

    public int getPopExitAnim() {
        return mPopExitAnim;
    }

    public int getPopEnterAnim() {
        return mPopEnterAnim;
    }

    public void putExtra(String name, Parcelable value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putParcelable(name, value);
    }

    public void putExtra(String name, String value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putString(name, value);
    }

    public void putExtra(String name, int value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putInt(name, value);
    }

    public void putExtra(String name, long value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putLong(name, value);
    }

    public void putExtra(String name, boolean value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putBoolean(name, value);
    }

    public void putExtra(String name, double value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putDouble(name, value);
    }

    public void putExtra(String name, float value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putFloat(name, value);
    }

    public void putExtras(Bundle extras) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putAll(extras);
    }

    public Bundle getExtras() {
        return mExtras;
    }

}