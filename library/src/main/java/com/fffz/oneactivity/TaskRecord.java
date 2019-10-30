package com.fffz.oneactivity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by fffz on 2017/12/21.
 */
class TaskRecord implements Parcelable {

    int mTaskTag;
    ArrayList<FragmentRecord> mFragmentRecords = new ArrayList<>();

    public TaskRecord(int taskTag) {
        mTaskTag = taskTag;
    }

    protected TaskRecord(Parcel in) {
        mTaskTag = in.readInt();
        mFragmentRecords = in.createTypedArrayList(FragmentRecord.CREATOR);
    }

    public static final Creator<TaskRecord> CREATOR = new Creator<TaskRecord>() {
        @Override
        public TaskRecord createFromParcel(Parcel in) {
            return new TaskRecord(in);
        }

        @Override
        public TaskRecord[] newArray(int size) {
            return new TaskRecord[size];
        }
    };

    public void add(FragmentRecord record) {
        mFragmentRecords.add(record);
    }

    public FragmentRecord pop() {
        return mFragmentRecords.remove(mFragmentRecords.size() - 1);
    }

    public ArrayList<FragmentRecord> getFragmentRecordsAbove(OneActivity activity, Class<? extends AbstractFragment> fragmentClass) {
        ArrayList<FragmentRecord> result = new ArrayList<>();
        for (int i = mFragmentRecords.size() - 1; i >= 0; i--) {
            FragmentRecord fragmentRecord = mFragmentRecords.get(i);
            if (fragmentRecord.getFragment(activity).getClass() == fragmentClass) {
                return result;
            }
            result.add(fragmentRecord);
        }
        return null;
    }

    public ArrayList<FragmentRecord> getFragmentRecordsExcept(OneActivity activity, Class<? extends AbstractFragment> fragmentClass) {
        boolean contains = false;
        ArrayList<FragmentRecord> result = new ArrayList<>();
        for (FragmentRecord fragmentRecord : mFragmentRecords) {
            if (fragmentRecord.getFragment(activity).getClass() == fragmentClass) {
                contains = true;
            } else {
                result.add(fragmentRecord);
            }
        }
        return contains ? result : null;
    }

    public FragmentRecord findFragmentRecord(OneActivity activity, Class<? extends AbstractFragment> fragmentClass) {
        for (FragmentRecord fragmentRecord : mFragmentRecords) {
            if (fragmentRecord.getFragment(activity).getClass() == fragmentClass) {
                return fragmentRecord;
            }
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTaskTag);
        dest.writeTypedList(mFragmentRecords);
    }

    public FragmentRecord getTopFragmentRecord() {
        for (int i = mFragmentRecords.size() - 1; i >= 0; i--) {
            FragmentRecord fragmentRecord = mFragmentRecords.get(i);
            if (!fragmentRecord.mHidden) {
                return fragmentRecord;
            }
        }
        return null;
    }

    public int getCount() {
        int count = 0;
        for (int i = mFragmentRecords.size() - 1; i >= 0; i--) {
            FragmentRecord fragmentRecord = mFragmentRecords.get(i);
            if (!fragmentRecord.mHidden) {
                count++;
            }
        }
        return count;
    }

    public void remove(FragmentRecord fragmentRecord) {
        mFragmentRecords.remove(fragmentRecord);
    }

}