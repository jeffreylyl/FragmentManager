package com.app.fm.base;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class RootFragment extends Fragment implements OnNewIntent {
    public int titleResId = -1;
    public Toolbar toolbar;
    public View.OnClickListener errorButtonClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleResId = getTitleResId();
    }

    public abstract int getTitleResId();

    public abstract int getFragmentLayoutId();

    public abstract void initViews(View view);

    public abstract void addLinsteners();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayoutId(), null);
        initViews(view);
        addLinsteners();
        handleOthers();
        return view;
    }

    protected View.OnClickListener getErrorButtonClickListener() {
        return errorButtonClickListener;
    }

    public void handleOthers() {
    }

    public void open(@NonNull RootFragment fragment) {
        getRoot().manager.addFragment(this, fragment, null);
    }

    public void open(@NonNull RootFragment fragment, Bundle bundle) {
        getRoot().manager.addFragment(this, fragment, bundle);
    }


    public void open(@NonNull RootFragment fragment, Bundle bundle, int stackMode) {
        getRoot().manager.addFragment(this, fragment, bundle, stackMode);
    }

    public void dialogFragment(Fragment to) {
        getRoot().manager.dialogFragment(to);
    }

    public void setDialogAnim(@AnimRes int dialog_in, @AnimRes int dialog_out) {
        getRoot().manager.setDialogAnim(dialog_in, dialog_out);
    }

    public void close() {
        getRoot().manager.close(this);
    }

    public void close(RootFragment fragment) {
        getRoot().manager.close(fragment);
    }

    public void closeUtilHome() {
        getRoot().manager.closeUtilHome();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            onNowHidden();
        } else {
            onNextShow();
        }
    }

    protected void onNowHidden() {
    }

    protected void onNextShow() {
    }

    public RootActivity getRoot() {
        FragmentActivity activity = getActivity();
        if (activity instanceof RootActivity) {
            return (RootActivity) activity;
        } else {
            throw new ClassCastException("this activity must extend RootActivity");
        }
    }

    @Override
    public void onNewIntent() {
    }

    public StackManager getStackManager() {
        return getRoot().manager;
    }

}
