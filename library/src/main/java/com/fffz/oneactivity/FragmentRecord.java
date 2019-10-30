package com.fffz.oneactivity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fffz on 2017/12/21.
 */
class FragmentRecord implements Parcelable {

    private static final AtomicInteger sAtomicInteger = new AtomicInteger();

    protected FragmentRecord(Parcel in) {
        mTag = in.readString();
        mClassName = in.readString();
        mLaunchMode = in.readInt();
        mIntent = in.readParcelable(FragmentIntent.class.getClassLoader());
        mArguments = in.readBundle();
        mHidden = in.readByte() != 0;
        mResultRecords = in.createTypedArrayList(ResultRecord.CREATOR);
    }

    public static final Creator<FragmentRecord> CREATOR = new Creator<FragmentRecord>() {
        @Override
        public FragmentRecord createFromParcel(Parcel in) {
            return new FragmentRecord(in);
        }

        @Override
        public FragmentRecord[] newArray(int size) {
            return new FragmentRecord[size];
        }
    };

    private static String generateFragmentTag(Object obj) {
        return System.identityHashCode(obj) + "@" + sAtomicInteger.getAndIncrement();
    }

    public TaskRecord mTaskRecord;
    private AbstractFragment mFragment;

    public String mTag;
    public String mClassName;
    public int mLaunchMode;
    public FragmentIntent mIntent;
    public Bundle mArguments;
    public boolean mHidden;
    public ArrayList<ResultRecord> mResultRecords = new ArrayList<>();

    public FragmentRecord(AbstractFragment fragment) {
        fragment.mFragmentRecord = this;
        mTag = generateFragmentTag(fragment);
        mFragment = fragment;
        mClassName = fragment.getClass().getName();
        mLaunchMode = fragment.getLaunchMode();
    }

    public void setFragment(AbstractFragment fragment) {
        mFragment = fragment;
        mFragment.mFragmentRecord = this;
    }

    public AbstractFragment getFragment(OneActivity activity) {
        if (mFragment == null) {
            try {
                mFragment = (AbstractFragment) Class.forName(mClassName).newInstance();
                mFragment.mActivity = activity;
                mFragment.mFragmentRecord = this;
                mFragment.setArguments(mArguments);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mFragment;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTag);
        dest.writeString(mClassName);
        dest.writeInt(mLaunchMode);
        dest.writeParcelable(mIntent, flags);
        dest.writeBundle(mArguments);
        dest.writeByte((byte) (mHidden ? 1 : 0));
        dest.writeTypedList(mResultRecords);
    }

}