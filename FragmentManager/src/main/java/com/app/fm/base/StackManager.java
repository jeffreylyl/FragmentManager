package com.app.fm.base;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.app.fm.listener.SimpleAnimationListener;
import com.app.fragment_manager.R;

public class StackManager implements CloseFragment {
    protected FragmentStack stack;
    protected final FragmentActivity context;
    protected long CLICK_SPACE = 500;
    protected long currentTime;
    protected int currentMode;
    protected int nextIn;
    protected int nextOut;
    protected int quitIn;
    protected int quitOut;
    protected Animation next_in;
    protected Animation next_out;
    protected int dialog_in;
    protected int dialog_out;
    public static final int STANDARD = 0x11;
    public static final int SINGLE_TOP = 0x12;
    public static final int SINGLE_TASK = 0x13;
    public static final int SINGLE_INSTANCE = 0x14;
    public static final int KEEP_CURRENT = 0x15;
    FragmentTransaction mTransaction;
    public static boolean isFirstClose = true;

    public void setClickSpace(long CLICK_SPACE) {
        this.CLICK_SPACE = CLICK_SPACE;
    }

    public StackManager(FragmentActivity context) {
        stack = new FragmentStack();
        stack.setCloseFragmentListener(this);
        this.context = context;
    }

    public void setFragment(@NonNull RootFragment mTargetFragment) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content_main, mTargetFragment, mTargetFragment.getClass().getName())
                .commit();
        stack.putStandard(mTargetFragment);
    }

    public void addFragment(@NonNull final Fragment from, @NonNull final Fragment to) {
        if (System.currentTimeMillis() - currentTime > CLICK_SPACE) {
            currentTime = System.currentTimeMillis();

            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
            if (nextIn != 0 && nextOut != 0 && quitIn != 0 && quitOut != 0) {
                transaction
                        .setCustomAnimations(nextIn, nextOut)
                        .add(R.id.content_main, to, to.getClass().getName())
                        .setCustomAnimations(nextIn, nextOut)
                        .hide(from)
                        .commit();
            } else {
                transaction
                        .add(R.id.content_main, to, to.getClass().getName())
                        .hide(from)
                        .commit();
            }

        }
    }

    /**
     * @param nextIn  The next page to enter the animation
     * @param nextOut The next page out of the animation
     * @param quitIn  The current page into the animation
     * @param quitOut Exit animation for the current page
     */
    public void setAnim(@AnimRes int nextIn, @AnimRes int nextOut, @AnimRes int quitIn, @AnimRes int quitOut) {
        this.nextIn = nextIn;
        this.nextOut = nextOut;
        this.quitIn = quitIn;
        this.quitOut = quitOut;
        next_in = AnimationUtils.loadAnimation(context, quitIn);
        next_out = AnimationUtils.loadAnimation(context, quitOut);
    }

    public void addFragment(RootFragment from, RootFragment to, Bundle bundle, @StackMode int stackMode) {
        if (stackMode != KEEP_CURRENT) {
            currentMode = stackMode;
        }
        if (bundle != null) {
            to.setArguments(bundle);
        }
        switch (currentMode) {
            case SINGLE_TOP:
                if (!stack.putSingleTop(to)) {
                    addFragment(from, to);
                }
                break;
            case SINGLE_TASK:
                if (!stack.putSingleTask(to)) {
                    addFragment(from, to);
                }
                break;
            case SINGLE_INSTANCE:
                stack.putSingleInstance(to);
                addFragment(from, to);
                break;
            default:
                stack.putStandard(to);
                addFragment(from, to);
                break;
        }
    }

    public void openFragment(RootFragment from, RootFragment to) {
        addFragment(from, to, null, KEEP_CURRENT);
    }

    public void addFragment(RootFragment from, RootFragment to, Bundle bundle) {
        addFragment(from, to, bundle, KEEP_CURRENT);
    }

    @Override
    public void beginTransaction() {
        if (mTransaction != null) {
            throw new RuntimeException("There is a transaction existing already !!!");
        }
        mTransaction = context.getSupportFragmentManager().beginTransaction();
    }

    @Override
    public void commit() {
        mTransaction.commit();
        mTransaction = null;
    }

    public void closeFragment(Fragment mTargetFragment) {
        if (mTransaction == null) {
            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
            transaction.remove(mTargetFragment).commit();
        } else {
            mTransaction.remove(mTargetFragment);
        }
    }

    public void closeUtilHome() {
        if (stack.isLast()) {
            return;
        }

        beginTransaction();

        isFirstClose = true;
        while (!stack.isLast()) {
            close(stack.getAndRemoveLastFragment());
        }

        RootFragment lastFragment = getCurrentFragment();
        show(lastFragment);

        commit();
    }

    public RootFragment closeUtilHomeAndReturn() {
        if (stack.isLast()) {
            return getCurrentFragment();
        }

        beginTransaction();

        while (!stack.isLast()) {
            closeFragment(stack.getAndRemoveLastFragment());
        }

        RootFragment lastFragment = getCurrentFragment();
        showFragment(lastFragment);

        commit();

        return lastFragment;
    }

    public RootFragment closeUtilLast(Integer animationId) {
        if (stack.isLast()) {
            return getCurrentFragment();
        }
        beginTransaction();

        closeLast(animationId);

        while (!stack.isLast()) {
            closeFragment(stack.getAndRemoveLastFragment());
        }

        RootFragment lastFragment = getCurrentFragment();
        showFragment(lastFragment);

        commit();
        return lastFragment;
    }

    public void closeLast() {
        closeFragment(stack.getAndRemoveLastFragment());
    }

    public void closeLast(Integer animationId) {
        if (!stack.isLast()) {
            if (next_out != null) {
                final Fragment fragment = stack.getAndRemoveLastFragment();
                Animation animation = null;

                if (animationId == null) {
                    animation = next_out;
                } else {
                    animation = AnimationUtils.loadAnimation(context, animationId.intValue());
                }
                fragment.getView().startAnimation(animation);
                animation.setAnimationListener(new SimpleAnimationListener() {
                    public void onAnimationEnd(Animation animation) {
                        closeFragment(fragment);
                    }
                });
            } else {
                closeLast();
            }
        }
    }

    @Override
    public void close(final RootFragment fragment) {
        if (isFirstClose) {
            View view = fragment.getView();
            if (view != null) {
                if (next_out != null) {
                    view.startAnimation(next_out);
                    next_out.setAnimationListener(new SimpleAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            closeFragment(fragment);
                        }
                    });
                } else {
                    closeFragment(fragment);
                }
            }
            isFirstClose = false;
        } else {
            closeFragment(fragment);
        }
    }

    public void closeFragment(String tag) {
        Fragment fragmentByTag = context.getSupportFragmentManager().findFragmentByTag(tag);
        if (fragmentByTag != null) {
            closeFragment(fragmentByTag);
            context.getSupportFragmentManager().popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void close() {
        context.getSupportFragmentManager().popBackStack();
    }

    public void closeAllFragment() {
        int backStackCount = context.getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            int backStackId = context.getSupportFragmentManager().getBackStackEntryAt(i).getId();
            context.getSupportFragmentManager().popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void showFragment(Fragment mTargetFragment) {
        if (mTransaction == null) {
            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
            transaction.show(mTargetFragment).commit();
        } else {
            mTransaction.show(mTargetFragment);
        }
    }

    @Override
    public void show(RootFragment fragment) {
        showFragment(fragment);
        View view = fragment.getView();
        if (view != null && next_in != null) {
            view.startAnimation(next_in);
        }
    }

    public void onBackPressed() {
        RootFragment[] last = stack.getLast();
        final RootFragment from = last[0];
        //intercept back event. if true, intercept back event and does not close
        if (from.onBackPressed()) {
            return;
        }
        final RootFragment to = last[1];
        if (from != null) {
            if (to != null) {
                FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
                transaction.show(to).commit();
                View fromView = from.getView();
                if (fromView != null && next_out != null) {
                    fromView.startAnimation(next_out);
                    next_out.setAnimationListener(new SimpleAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            stack.onBackPressed();
                            closeFragment(from);
                        }
                    });

                } else {
                    stack.onBackPressed();
                    closeFragment(from);
                }
            }
        }
        if (to != null) {
            View toView = to.getView();
            if (toView != null && next_in != null) {
                toView.startAnimation(next_in);
            }
        } else {
            closeAllFragment();
            context.finish();
        }
    }

    public void dialogFragment(Fragment to) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        if (!to.isAdded()) {
            if (dialog_in != 0 && dialog_out != 0) {
                transaction
                        .setCustomAnimations(dialog_in, dialog_out)
                        .add(R.id.content_main, to, to.getClass().getName())
                        .commit();
            } else {
                transaction
                        .add(R.id.content_main, to, to.getClass().getName())
                        .commit();
            }

        }
    }

    public void setDialogAnim(@AnimRes int dialog_in, @AnimRes int dialog_out) {
        this.dialog_in = dialog_in;
        this.dialog_out = dialog_out;
    }

    public RootFragment getCurrentFragment() {
        return (RootFragment) stack.getCurrentFragment();
    }

    @IntDef({STANDARD, SINGLE_TOP, SINGLE_TASK, SINGLE_INSTANCE, KEEP_CURRENT})
    public @interface StackMode {

    }

}
