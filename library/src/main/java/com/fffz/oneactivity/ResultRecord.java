package com.fffz.oneactivity;

import android.os.Parcel;
import android.os.Parcelable;

class ResultRecord implements Parcelable {
    public final String mFragmentTag;
    public final int mRequestCode;
    public final FragmentResultCallback mCallback;

    public ResultRecord(String fragmentTag, int requestCode, FragmentResultCallback callback) {
        mFragmentTag = fragmentTag;
        mRequestCode = requestCode;
        mCallback = callback;
    }

    protected ResultRecord(Parcel in) {
        mFragmentTag = in.readString();
        mRequestCode = in.readInt();
        mCallback = null;
    }

    public static final Creator<ResultRecord> CREATOR = new Creator<ResultRecord>() {
        @Override
        public ResultRecord createFromParcel(Parcel in) {
            return new ResultRecord(in);
        }

        @Override
        public ResultRecord[] newArray(int size) {
            return new ResultRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFragmentTag);
        dest.writeInt(mRequestCode);
    }
}