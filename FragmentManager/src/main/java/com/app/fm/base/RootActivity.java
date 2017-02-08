package com.app.fm.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.app.fm.util.StringUtil;

public abstract class RootActivity extends AppCompatActivity {

    public StackManager manager;
    public KeyCallBack callBack;
    public Context context;
    public StringUtil mStringUtil;

    public abstract int getLayoutId();
    public abstract void initViews();
    public abstract void addListeners();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       
        setContentView(getLayoutId());

        context = this;
        mStringUtil = StringUtil.getInstance(this);

        initStackManager();

        onCreateNow(savedInstanceState);
        
    }

    public void initStackManager() {
        manager = new StackManager(this);
        manager.setFragment(getRootFragment());
    }

    public void onCreateNow(Bundle savedInstanceState) {
        initViews();
        addListeners();
    }

    public abstract
    @NonNull
    RootFragment getRootFragment();

    public void setAnim(@AnimRes int nextIn, @AnimRes int nextOut, @AnimRes int quitIn, @AnimRes int quitOut) {
        manager.setAnim(nextIn, nextOut, quitIn, quitOut);
    }

    @Override
    public final boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                manager.onBackPressed();
                return true;
            default:
                if (callBack != null) {
                    return callBack.onKeyDown(keyCode, event);
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setKeyCallBack(KeyCallBack callBack) {
        this.callBack = callBack;
    }
}
