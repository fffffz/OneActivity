package com.fffz.oneactivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fffz on 2017/12/26.
 */
public abstract class OneActivity<T extends AbstractFragment> extends AppCompatActivity {

    public static final int STANDARD = AbstractFragment.STANDARD;
    public static final int SINGLE_TOP = AbstractFragment.SINGLE_TOP;
    public static final int SINGLE_TASK = AbstractFragment.SINGLE_TASK;
    public static final int SINGLE_INSTANCE = AbstractFragment.SINGLE_INSTANCE;

    private static final String KEY_TASK_HISTORY = "TaskHistory";
    private static final int VALUE_NOT_SET = Integer.MIN_VALUE;

    private ArrayList<FragmentLifecycleCallbacks> mFragmentLifecycleCallbacks = new ArrayList<>();
    private ArrayList<TaskRecord> mTasks;
    private final int mContainerId;
    private FragmentManager mFragmentManager;
    protected int mCurrentTaskTag = VALUE_NOT_SET;
    protected Handler mHandler = new Handler();

    public OneActivity() {
        mContainerId = getContainerId();
    }

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
        int layoutId = getLayoutId();
        if (layoutId != -1) {
            setContentView(layoutId);
        }
        if (savedInstanceState == null) {
            mTasks = new ArrayList<>();
            return;
        }
        mTasks = savedInstanceState.getParcelableArrayList(KEY_TASK_HISTORY);
        if (mTasks == null) {
            mTasks = new ArrayList<>();
            return;
        }
        for (TaskRecord taskRecord : mTasks) {
            for (FragmentRecord fragmentRecord : taskRecord.mFragmentRecords) {
                fragmentRecord.mTaskRecord = taskRecord;
            }
        }
        if (mTasks.size() > 0) {
            showTask(mTasks.get(mTasks.size() - 1).mTaskTag);
        }
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState,
                               @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    protected int getLayoutId() {
        return -1;
    }

    protected int getContainerId() {
        return android.R.id.content;
    }

    public int getCurrentTaskTag() {
        return mCurrentTaskTag;
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_TASK_HISTORY, mTasks);
    }

    @CallSuper
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    public void onBackPressed() {
        TaskRecord taskRecord = getTopTask();
        if (taskRecord == null) {
            super.onBackPressed();
            return;
        }
        if (taskRecord.getCount() <= 1) {
            super.onBackPressed();
            return;
        }
        FragmentRecord topFragmentRecord = taskRecord.getTopFragmentRecord();
        AbstractFragment topFragment = topFragmentRecord.getFragment(this);
        if (topFragment.onBackPressed()) {
            return;
        }
        finishFragment((T) topFragment);
    }

    public synchronized void addTask(int taskTag, T rootFragment) {
        if (isTaskAdded(taskTag)) {
            return;
        }
        rootFragment.mActivity = this;
        TaskRecord taskRecord = new TaskRecord(taskTag);
        FragmentRecord fragmentRecord = new FragmentRecord(rootFragment);
        fragmentRecord.mTaskRecord = taskRecord;
        fragmentRecord.mArguments = rootFragment.getArguments();
        taskRecord.add(fragmentRecord);
        mTasks.add(taskRecord);
    }

    public synchronized void clearTask(int taskTag) {
        TaskRecord taskRecord = findTask(taskTag);
        if (taskRecord == null || taskRecord.mFragmentRecords == null || taskRecord.mFragmentRecords.size() < 2) {
            return;
        }
        FragmentRecord rootFragmentRecord = taskRecord.mFragmentRecords.get(0);
        boolean isTopFragment = isTopFragment(rootFragmentRecord);
        FragmentTransaction transition = mFragmentManager.beginTransaction();
        for (int i = taskRecord.mFragmentRecords.size() - 1; i >= 1; i--) {
            transition.remove(taskRecord.mFragmentRecords.get(i).getFragment(this));
        }
        taskRecord.mFragmentRecords.clear();
        taskRecord.mFragmentRecords.add(rootFragmentRecord);
        transition.commitNow();
        if (!isTopFragment) {
            rootFragmentRecord.getFragment(this).dispatchOnResumeView();
        }
    }

    public synchronized void showTask(int taskTag) {
        if (mCurrentTaskTag == taskTag) {
            return;
        }
        AbstractFragment topFragment = getTopFragment();
        if (topFragment != null) {
            topFragment.dispatchOnPauseView();
        }
        TaskRecord topTaskRecord2 = null;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (TaskRecord taskRecord : mTasks) {
            if (taskRecord.mTaskTag == taskTag) {
                topTaskRecord2 = taskRecord;
                AbstractFragment topFragment2 = topTaskRecord2.getTopFragmentRecord().getFragment(this);
                for (FragmentRecord fragmentRecord : taskRecord.mFragmentRecords) {
                    if (fragmentRecord.mHidden) {
                        continue;
                    }
                    AbstractFragment fragment = fragmentRecord.getFragment(this);
                    if (mFragmentManager.findFragmentByTag(fragmentRecord.mTag) == null) {
                        transaction.add(mContainerId, fragment, fragmentRecord.mTag);
                    } else {
                        transaction.show(fragment);
                        if (fragment == topFragment2) {
                            fragment.dispatchOnResumeView();
                        }
                    }
                }
            } else {
                for (FragmentRecord fragmentRecord : taskRecord.mFragmentRecords) {
                    AbstractFragment fragment = fragmentRecord.getFragment(this);
                    transaction.hide(fragment);
                }
            }
        }
        if (!commitTransaction(transaction)) {
            return;
        }
        if (topTaskRecord2 == null) {
            throw new IllegalStateException("Task " + taskTag + " doesn't exist, please addTask first");
        }
        mTasks.remove(topTaskRecord2);
        mTasks.add(topTaskRecord2);
        mCurrentTaskTag = taskTag;
    }

    public final void finishFragment(T fragment) {
        finishFragment(fragment, VALUE_NOT_SET, VALUE_NOT_SET);
    }

    @CallSuper
    public void finishFragment(T fragment, int enterAnim, int exitAnim) {
        finishFragment(fragment.mFragmentRecord, enterAnim, exitAnim);
    }

    public final void showFragment(T fragment) {
        showFragment(fragment.mFragmentRecord);
    }

    public final void hideFragment(T fragment) {
        hideFragment(fragment.mFragmentRecord);
    }

    public final T getTopFragment() {
        FragmentRecord topFragmentRecord = getTopFragmentRecord();
        if (topFragmentRecord == null) {
            return null;
        }
        return (T) topFragmentRecord.getFragment(this);
    }

    public final List<T> getFragments(int taskTag) {
        return getFragments(taskTag, true);
    }

    public final List<T> getFragments(int taskTag, boolean containsHidden) {
        for (TaskRecord taskRecord : mTasks) {
            if (taskRecord.mTaskTag == taskTag) {
                ArrayList<FragmentRecord> fragmentRecords = taskRecord.mFragmentRecords;
                if (fragmentRecords == null) {
                    return null;
                }
                ArrayList<T> fragments = new ArrayList<>();
                if (containsHidden) {
                    for (FragmentRecord fragmentRecord : fragmentRecords) {
                        fragments.add((T) fragmentRecord.getFragment(this));
                    }
                } else {
                    for (FragmentRecord fragmentRecord : fragmentRecords) {
                        if (!fragmentRecord.mHidden) {
                            fragments.add((T) fragmentRecord.getFragment(this));
                        }
                    }
                }
                return fragments;
            }
        }
        return null;
    }

    public final boolean isTopFragment(Class<? extends T> fragment) {
        T topFragment = getTopFragment();
        return topFragment != null && topFragment.getClass() == fragment;
    }

    public final boolean isTopFragment(T fragment) {
        return isTopFragment(fragment.mFragmentRecord);
    }

    private boolean isTopFragment(FragmentRecord fragmentRecord) {
        FragmentRecord topFragmentRecord = getTopFragmentRecord();
        if (topFragmentRecord == null) {
            return false;
        }
        return topFragmentRecord == fragmentRecord ||
                topFragmentRecord.mTag.equals(fragmentRecord.mTag);
    }

    private TaskRecord getTopTask() {
        if (mCurrentTaskTag == VALUE_NOT_SET) {
            return null;
        }
        return findTask(mCurrentTaskTag);
    }

    private FragmentRecord getTopFragmentRecord() {
        TaskRecord topTask = getTopTask();
        if (topTask == null) {
            return null;
        }
        return topTask.getTopFragmentRecord();
    }

    void finishFragment(FragmentRecord finishRecord) {
        finishFragment(finishRecord, VALUE_NOT_SET, VALUE_NOT_SET);
    }

    void finishFragment(final FragmentRecord finishRecord, final int enterAnim, final int exitAnim) {
        if (finishRecord == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                realFinishFragment(finishRecord, enterAnim, exitAnim);
            }
        });
    }

    private synchronized void realFinishFragment(FragmentRecord finishRecord, int enterAnim, int exitAnim) {
        TaskRecord taskRecord = finishRecord.mTaskRecord;
        if (taskRecord.mFragmentRecords.size() <= 1) {
            finish();
            return;
        }
        boolean isTop = isTopFragment(finishRecord);
        taskRecord.remove(finishRecord);
        AbstractFragment finishFragment = finishRecord.getFragment(this);
        finishFragment.onPopExit();
        if (taskRecord.getCount() == 0) {
            if (taskRecord.mFragmentRecords.size() == 0) {
                mTasks.remove(taskRecord);
            }
            if (mTasks.size() == 0) {
                finish();
                return;
            }
            taskRecord = getTopTask();
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction().disallowAddToBackStack();
        if (isTop) {
            FragmentRecord resumeRecord = taskRecord.getTopFragmentRecord();
            AbstractFragment resumeFragment = (AbstractFragment) mFragmentManager.findFragmentByTag(resumeRecord.mTag);
            if (exitAnim == VALUE_NOT_SET) {
                exitAnim = finishRecord.mIntent.getPopExitAnim();
                if (exitAnim < 0) {
                    int[] animations = finishFragment.getAnimations();
                    if (animations != null && animations.length == 4) {
                        exitAnim = animations[3];
                    }
                }
            }
            exitAnim = Math.max(exitAnim, 0);
            if (resumeFragment != null) {
                enterAnim = getEnterAnim(enterAnim, finishRecord, resumeFragment);
                if (resumeFragment.isHidden()) {
                    transaction.show(resumeFragment);
                    resumeFragment.startAnimation(enterAnim);
                }
            } else {
                resumeFragment = resumeRecord.getFragment(this);
                enterAnim = getEnterAnim(enterAnim, finishRecord, resumeFragment);
                int containerId = 0;
                if (resumeRecord.mIntent != null) {
                    containerId = resumeRecord.mIntent.getContainerId();
                }
                if (containerId <= 0) {
                    containerId = mContainerId;
                }
                transaction.add(containerId, resumeFragment, resumeRecord.mTag);
            }
            finishFragment.dispatchOnPauseView();
            transaction.setCustomAnimations(enterAnim, exitAnim);
            transaction.remove(finishFragment);
            if (!commitTransaction(transaction)) {
                return;
            }
            if (resumeFragment.isAdded()) {
                resumeFragment.dispatchOnResumeView();
                resumeFragment.onPopEnter();
            } else {
                final AbstractFragment finalResumeFragment = resumeFragment;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finalResumeFragment.dispatchOnResumeView();
                        finalResumeFragment.onPopEnter();
                    }
                });
            }
        } else {
            transaction.remove(finishFragment);
            if (!commitTransaction(transaction)) {
                return;
            }
        }
        for (ResultRecord resultRecord : finishRecord.mResultRecords) {
            FragmentRecord resultFragmentRecord = findFragmentRecord(resultRecord.mFragmentTag);
            if (resultFragmentRecord == null) {
                continue;
            }
            if (resultRecord.mRequestCode != -1) {
                AbstractFragment resultFragment = resultFragmentRecord.getFragment(this);
                resultFragment.onFragmentResult(resultRecord.mRequestCode,
                        finishFragment.mResultCode,
                        finishFragment.mResultData);
            } else if (resultRecord.mCallback != null) {
                resultRecord.mCallback.onFragmentResult(finishFragment.mResultCode, finishFragment.mResultData2);
            }
        }
    }

    private int getEnterAnim(int enterAnim, FragmentRecord finishRecord, AbstractFragment resumeFragment) {
        if (enterAnim == VALUE_NOT_SET) {
            enterAnim = finishRecord.mIntent.getPopEnterAnim();
            if (enterAnim < 0) {
                int[] animations = resumeFragment.getAnimations();
                if (animations != null && animations.length == 4) {
                    enterAnim = animations[2];
                }
            }
        }
        enterAnim = Math.max(enterAnim, 0);
        return enterAnim;
    }

    void showFragment(FragmentRecord fragmentRecord) {
        FragmentRecord topFragmentRecord = getTopFragmentRecord();
        TaskRecord fromTaskRecord = topFragmentRecord.mTaskRecord;
        fragmentRecord.mTaskRecord.remove(fragmentRecord);
        fromTaskRecord.add(fragmentRecord);
        startFragmentInternal(topFragmentRecord, topFragmentRecord, fragmentRecord, -1, null, false);
    }

    public final void setFragmentHidden(T fragment, boolean hidden) {
        fragment.mFragmentRecord.mHidden = hidden;
    }

    void hideFragment(final FragmentRecord hideRecord) {
        if (hideRecord == null || hideRecord.mHidden) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                realHideFragment(hideRecord);
            }
        });
    }

    private synchronized void realHideFragment(FragmentRecord hideRecord) {
        boolean isTop = isTopFragment(hideRecord);
        TaskRecord taskRecord = hideRecord.mTaskRecord;
        hideRecord.mHidden = true;
        AbstractFragment hideFragment = hideRecord.getFragment(this);
        hideFragment.onPopExit();
        hideFragment.dispatchOnPauseView();
        FragmentTransaction transaction = mFragmentManager.beginTransaction().disallowAddToBackStack();
        int popExitAnim = hideRecord.mIntent.getPopExitAnim();
        if (popExitAnim < 0) {
            int[] animations = hideFragment.getAnimations();
            if (animations != null && animations.length == 4) {
                popExitAnim = animations[3];
            }
        }
        popExitAnim = Math.max(popExitAnim, 0);
        if (isTop) {
            FragmentRecord resumeRecord = taskRecord.getTopFragmentRecord();
            AbstractFragment resumeFragment = (AbstractFragment) mFragmentManager.findFragmentByTag(resumeRecord.mTag);
            if (resumeFragment != null) {
                transaction.show(resumeFragment);
            } else {
                resumeFragment = resumeRecord.getFragment(this);
                int containerId = 0;
                if (resumeRecord.mIntent != null) {
                    containerId = resumeRecord.mIntent.getContainerId();
                }
                if (containerId <= 0) {
                    containerId = mContainerId;
                }
                transaction.add(containerId, resumeFragment, resumeRecord.mTag);
            }
            int popEnterAnim = hideRecord.mIntent.getPopEnterAnim();
            if (popEnterAnim < 0) {
                int[] animations = resumeFragment.getAnimations();
                if (animations != null && animations.length == 4) {
                    popEnterAnim = animations[2];
                }
            }
            popEnterAnim = Math.max(popEnterAnim, 0);
            transaction.setCustomAnimations(popEnterAnim, popExitAnim);
            transaction.hide(hideFragment);
            hideFragment.startAnimation(popExitAnim);
            if (!commitTransaction(transaction)) {
                return;
            }
            if (resumeFragment.isAdded()) {
                resumeFragment.dispatchOnResumeView();
                resumeFragment.onPopEnter();
            } else {
                final AbstractFragment finalResumeFragment = resumeFragment;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finalResumeFragment.dispatchOnResumeView();
                        finalResumeFragment.onPopEnter();
                    }
                });
            }
        } else {
            transaction.hide(hideFragment);
            hideFragment.startAnimation(popExitAnim);
            if (!commitTransaction(transaction)) {
                return;
            }
        }
    }

    public final void startFragment(Class<? extends T> toFragmentClass) {
        FragmentIntent intent = new FragmentIntent(getTopFragment(), toFragmentClass);
        startFragmentForResult(intent, -1, null);
    }

    public final void startFragment(FragmentIntent intent) {
        startFragmentForResult(intent, -1, null);
    }

    public final void startFragment(T from, Class<? extends T> toFragmentClass) {
        FragmentIntent intent = new FragmentIntent(from, toFragmentClass);
        intent.setLaunchAction(FragmentIntent.ACTION_NONE);
        startFragmentForResult(intent, -1, null);
    }

    public final void startFragment(T from, Class<? extends T> toFragmentClass,
                                    @FragmentIntent.LaunchAction int launchAction) {
        FragmentIntent intent = new FragmentIntent(from, toFragmentClass);
        intent.setLaunchAction(launchAction);
        startFragmentForResult(intent, -1, null);
    }

    public final void startFragmentForResult(FragmentIntent intent, int requestCode) {
        checkForValidRequestCode(requestCode);
        startFragmentForResult(intent, requestCode, null);
    }

    public final void startFragmentForResult(FragmentIntent intent, FragmentResultCallback resultCallback) {
        startFragmentForResult(intent, -1, resultCallback);
    }

    public final void startFragmentForResult(T from, Class<? extends T> toFragmentClass, int requestCode) {
        checkForValidRequestCode(requestCode);
        FragmentIntent intent = new FragmentIntent(from, toFragmentClass);
        intent.setLaunchAction(FragmentIntent.ACTION_NONE);
        startFragmentForResult(intent, requestCode, null);
    }

    public final void startFragmentForResult(T from,
                                             Class<? extends T> toFragmentClass,
                                             FragmentResultCallback resultCallback) {
        FragmentIntent intent = new FragmentIntent(from, toFragmentClass);
        intent.setLaunchAction(FragmentIntent.ACTION_NONE);
        startFragmentForResult(intent, -1, resultCallback);
    }

    public final void startFragmentForResult(T from,
                                             Class<? extends T> toFragmentClass,
                                             @FragmentIntent.LaunchAction int launchAction,
                                             int requestCode) {
        checkForValidRequestCode(requestCode);
        FragmentIntent intent = new FragmentIntent(from, toFragmentClass);
        intent.setLaunchAction(launchAction);
        startFragmentForResult(intent, requestCode, null);
    }

    public final void startFragmentForResult(T from, Class<? extends T> toFragmentClass,
                                             @FragmentIntent.LaunchAction int launchAction,
                                             FragmentResultCallback resultCallback) {
        FragmentIntent intent = new FragmentIntent(from, toFragmentClass);
        intent.setLaunchAction(launchAction);
        startFragmentForResult(intent, -1, resultCallback);
    }

    @CallSuper
    public void startFragmentForResult(final FragmentIntent intent, final int requestCode, final FragmentResultCallback resultCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                realStartFragmentForResult(intent, requestCode, resultCallback);
            }
        });
    }

    private synchronized void realStartFragmentForResult(FragmentIntent intent, int requestCode, FragmentResultCallback resultCallback) {
        AbstractFragment from = intent.getFrom();
        FragmentRecord fromFragmentRecord = getFragmentRecord(from);
        if (fromFragmentRecord == null) {
            throw new IllegalArgumentException("Invalid fragment! " + from + " not in fragment stack");
        }
        TaskRecord fromTaskRecord = fromFragmentRecord.mTaskRecord;
        if (fromTaskRecord.mTaskTag != mCurrentTaskTag) {
            return;
        }
        int launchAction = intent.getLaunchAction();
        Class<? extends AbstractFragment> toFragmentClass = intent.getTo();
        if (launchAction == FragmentIntent.ACTION_CLEAR_TOP) {
            ArrayList<FragmentRecord> fragmentRecords = fromTaskRecord.getFragmentRecordsAbove(this, toFragmentClass);
            if (fragmentRecords != null) {
                for (int i = 0; i < fragmentRecords.size() - 1; i++) {
                    finishFragment(fragmentRecords.get(i));
                }
            }
        } else if (launchAction == FragmentIntent.ACTION_CLEAR_TASK) {
            ArrayList<FragmentRecord> fragmentRecords = fromTaskRecord.getFragmentRecordsExcept(this, toFragmentClass);
            if (fragmentRecords != null) {
                for (int i = 0; i < fragmentRecords.size() - 1; i++) {
                    finishFragment(fragmentRecords.get(i));
                }
            }
        }
        if (handleLaunchMode(intent, fromFragmentRecord, requestCode, resultCallback)) {
            return;
        }
        standard(intent, fromFragmentRecord, requestCode, resultCallback);
    }

    @Nullable
    private static FragmentRecord getFragmentRecord(AbstractFragment from) {
        FragmentRecord fromFragmentRecord = from.mFragmentRecord;
        if (fromFragmentRecord != null) {
            return fromFragmentRecord;
        }
        Fragment parent = from.getParentFragment();
        if (parent instanceof AbstractFragment) {
            return getFragmentRecord((AbstractFragment) parent);
        }
        return null;
    }

    private boolean handleLaunchMode(FragmentIntent intent, FragmentRecord fromFragmentRecord, int requestCode, FragmentResultCallback resultCallback) {
        TaskRecord fromTaskRecord = fromFragmentRecord.mTaskRecord;
        FragmentRecord topFragmentRecord = fromTaskRecord.getTopFragmentRecord();
        Class<? extends AbstractFragment> toFragmentClass = intent.getTo();
        if (topFragmentRecord.getFragment(this).getClass() == toFragmentClass && (topFragmentRecord.mLaunchMode == SINGLE_TOP || topFragmentRecord.mLaunchMode == SINGLE_TASK || topFragmentRecord.mLaunchMode == SINGLE_INSTANCE)) {
            topFragmentRecord.mIntent = intent;
            topFragmentRecord.mArguments = intent.getExtras();
            startFragmentInternal(topFragmentRecord, fromFragmentRecord, topFragmentRecord, requestCode, resultCallback, true);
            return true;
        }
        FragmentRecord fragmentRecord = fromTaskRecord.findFragmentRecord(this, toFragmentClass);
        if (fragmentRecord != null && fragmentRecord.mLaunchMode == SINGLE_TASK) {
            for (int i = fragmentRecord.mTaskRecord.mFragmentRecords.size() - 1; i >= 0; i--) {
                FragmentRecord fragmentRecord2 = fragmentRecord.mTaskRecord.mFragmentRecords.get(i);
                AbstractFragment fragment = fragmentRecord2.getFragment(this);
                if (fragment.getClass() == toFragmentClass) {
                    break;
                }
                realFinishFragment(fragmentRecord2, VALUE_NOT_SET, VALUE_NOT_SET);
            }
            fragmentRecord.mIntent = intent;
            fragmentRecord.mArguments = intent.getExtras();
            startFragmentInternal(topFragmentRecord, fromFragmentRecord, fragmentRecord, requestCode, resultCallback, true);
            return true;
        }
        for (TaskRecord taskRecord : mTasks) {
            fragmentRecord = taskRecord.findFragmentRecord(this, toFragmentClass);
            if (fragmentRecord != null && fragmentRecord.mLaunchMode == SINGLE_INSTANCE) {
                taskRecord.remove(fragmentRecord);
                fromTaskRecord.add(fragmentRecord);
                fragmentRecord.mIntent = intent;
                fragmentRecord.mArguments = intent.getExtras();
                startFragmentInternal(topFragmentRecord, fromFragmentRecord, fragmentRecord, requestCode, resultCallback, true);
                return true;
            }
        }
        return false;
    }

    private void standard(FragmentIntent intent, FragmentRecord fromFragmentRecord, int requestCode, FragmentResultCallback resultCallback) {
        FragmentRecord topFragmentRecord = getTopFragmentRecord();
        FragmentRecord toFragmentRecord = generateFragmentRecord(intent);
        TaskRecord fromTaskRecord = fromFragmentRecord.mTaskRecord;
        fromTaskRecord.add(toFragmentRecord);
        startFragmentInternal(topFragmentRecord, fromFragmentRecord, toFragmentRecord, requestCode, resultCallback, true);
    }

    @NonNull
    private FragmentRecord generateFragmentRecord(FragmentIntent intent) {
        AbstractFragment fragment = null;
        try {
            fragment = intent.getTo().newInstance();
            fragment.mActivity = this;
            fragment.setArguments(intent.getExtras());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FragmentRecord toFragmentRecord = new FragmentRecord(fragment);
        toFragmentRecord.mIntent = intent;
        toFragmentRecord.mArguments = intent.getExtras();
        return toFragmentRecord;
    }

    private void startFragmentInternal(FragmentRecord topFragmentRecord, FragmentRecord fromFragmentRecord,
                                       FragmentRecord toFragmentRecord, int requestCode, FragmentResultCallback resultCallback, boolean add) {
        FragmentIntent intent = toFragmentRecord.mIntent;
        AbstractFragment toFragment = toFragmentRecord.getFragment(this);
        if (topFragmentRecord == toFragmentRecord) {
            toFragment.onNewIntent(intent);
            return;
        }
        toFragmentRecord.mHidden = false;
        if (requestCode != -1 || resultCallback != null) {
            toFragmentRecord.mResultRecords.add(new ResultRecord(fromFragmentRecord.mTag, requestCode, resultCallback));
        }
        toFragmentRecord.mTaskRecord = fromFragmentRecord.mTaskRecord;
        AbstractFragment topFragment = topFragmentRecord.getFragment(this);
        topFragment.onPushExit();
        topFragment.dispatchOnPauseView();
        int pushEnterAnim = intent.getPushEnterAnim();
        if (pushEnterAnim < 0) {
            int[] animations = toFragment.getAnimations();
            if (animations != null && animations.length == 4) {
                pushEnterAnim = animations[0];
            }
        }
        int pushExitAnim = intent.getPushExitAnim();
        if (pushExitAnim < 0) {
            int[] animations = topFragment.getAnimations();
            if (animations != null && animations.length == 4) {
                pushExitAnim = animations[1];
            }
        }
        pushEnterAnim = Math.max(pushEnterAnim, 0);
        pushExitAnim = Math.max(pushExitAnim, 0);
        FragmentTransaction transaction = mFragmentManager.beginTransaction()
                .disallowAddToBackStack()
                .setCustomAnimations(pushEnterAnim, pushExitAnim);
        if (add) {
            Fragment fragment = mFragmentManager.findFragmentByTag(toFragmentRecord.mTag);
            if (fragment == null) {
                int containerId = intent.getContainerId();
                if (containerId <= 0) {
                    containerId = mContainerId;
                }
                transaction.add(containerId, toFragment, toFragmentRecord.mTag);
            } else {
                add = false;
                transaction.show(toFragment);
                toFragment.onNewIntent(intent);
            }
        } else {
            transaction.show(toFragment);
        }
        if (intent.mShouldHideTopFragment == 1) {
            transaction.hide(topFragment);
        }
        commitTransaction(transaction);
        if (!add) {
            toFragment.dispatchOnResumeView();
            toFragment.onPushEnter();
        }
    }

    private boolean commitTransaction(FragmentTransaction transaction) {
        try {
            transaction.commitNow();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void checkForValidRequestCode(int requestCode) {
        if (requestCode < 0) {
            throw new IllegalArgumentException("RequestCode must be greater than or equal to zero");
        }
    }

    public boolean isTaskAdded(int taskTag) {
        return findTask(taskTag) != null;
    }

    private TaskRecord findTask(int taskTag) {
        for (TaskRecord taskRecord : mTasks) {
            if (taskRecord.mTaskTag == taskTag) {
                return taskRecord;
            }
        }
        return null;
    }

    FragmentRecord findFragmentRecord(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        for (TaskRecord taskRecord : mTasks) {
            for (FragmentRecord record : taskRecord.mFragmentRecords) {
                if (record.mTag.equals(tag)) {
                    return record;
                }
            }
        }
        return null;
    }

    public <E extends T> E findFragmentByClass(Class<E> clazz) {
        for (int i = mTasks.size() - 1; i >= 0; i--) {
            TaskRecord taskRecord = mTasks.get(i);
            for (int j = taskRecord.mFragmentRecords.size() - 1; j >= 0; j--) {
                FragmentRecord fragmentRecord = taskRecord.mFragmentRecords.get(j);
                if (TextUtils.equals(fragmentRecord.mClassName, clazz.getName())) {
                    return (E) fragmentRecord.getFragment(this);
                }
            }
        }
        return null;
    }

    public void registerFragmentLifecycleCallbacks(FragmentLifecycleCallbacks callback) {
        synchronized (mFragmentLifecycleCallbacks) {
            mFragmentLifecycleCallbacks.add(callback);
        }
    }

    public void unregisterFragmentLifecycleCallbacks(FragmentLifecycleCallbacks callback) {
        synchronized (mFragmentLifecycleCallbacks) {
            mFragmentLifecycleCallbacks.remove(callback);
        }
    }

    void dispatchFragmentOnCreateView(T fragment, LayoutInflater inflater, @Nullable ViewGroup container,
                                      @Nullable Bundle savedInstanceState) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onCreateView(fragment, inflater, container, savedInstanceState);
        }
    }

    void dispatchFragmentOnViewCreated(T fragment, View view, @Nullable Bundle savedInstanceState) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onViewCreated(fragment, view, savedInstanceState);
        }
    }

    void dispatchFragmentOnNewIntent(T fragment, FragmentIntent intent) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onNewIntent(fragment, intent);
        }
    }

    void dispatchFragmentOnResumeView(T fragment) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onResumeView(fragment);
        }
    }

    void dispatchFragmentOnPauseView(T fragment) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onPauseView(fragment);
        }
    }

    void dispatchFragmentOnDestroyView(T fragment) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onDestroyView(fragment);
        }
    }

    void dispatchFragmentOnPushEnter(T fragment) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onPushEnter(fragment);
        }
    }

    void dispatchFragmentOnPushExit(T fragment) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onPushExit(fragment);
        }
    }

    void dispatchFragmentOnPopEnter(T fragment) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onPopEnter(fragment);
        }
    }

    void dispatchFragmentOnPopExit(T fragment) {
        for (FragmentLifecycleCallbacks callback : mFragmentLifecycleCallbacks) {
            callback.onPopExit(fragment);
        }
    }

}