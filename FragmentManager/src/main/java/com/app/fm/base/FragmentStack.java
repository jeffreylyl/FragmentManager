package com.app.fm.base;

import android.support.v4.app.Fragment;

import java.util.ArrayList;

/*
     The stack to manager the fragments.
 */
public class FragmentStack {
    protected ArrayList<ArrayList<RootFragment>> stackList = new ArrayList<>();
    protected ArrayList<RootFragment> stack;
    protected CloseFragment listener;

    public FragmentStack() {
        if (stack == null) {
            stack = new ArrayList<>();
        }
        stackList.add(stack);
    }


    public void putStandard(RootFragment fragment) {
        stackList.get(stackList.size() - 1).add(fragment);
    }

    public boolean putSingleTop(RootFragment fragment) {
        ArrayList<RootFragment> lastList = stackList.get(stackList.size() - 1);
        if (lastList.isEmpty()) {
            lastList.add(fragment);
            return false;
        } else {
            RootFragment last = lastList.get(lastList.size() - 1);
            if (last.getClass().getName().equals(fragment.getClass().getName())) {
                fragment.onNewIntent();
                return true;
            } else {
                lastList.add(fragment);
                return false;
            }
        }
    }

    public boolean putSingleTask(RootFragment fragment) {
        boolean isClear = false;
        ArrayList<RootFragment> lastList = stackList.get(stackList.size() - 1);
        if (lastList.isEmpty()) {
            lastList.add(fragment);
        } else {
            int tempIndex = 0;
            for (int x = 0; x <= lastList.size() - 1; x++) {
                if (lastList.get(x).getClass().getName().equals(fragment.getClass().getName())) {
                    //clear all instance
                    isClear = true;
                    tempIndex = x;
                    break;
                }
            }
            if (!isClear) {
                lastList.add(fragment);
            } else {
                if (listener != null) {
                    listener.show(lastList.get(tempIndex));
                    StackManager.isFirstClose = true;
                    for (int i = lastList.size() - 1; i > tempIndex; i--) {
                        listener.close(lastList.get(i));
                    }
                    for (int j = lastList.size() - 1; j > tempIndex; j--) {
                        lastList.remove(j);
                    }
                }

            }
        }
        return isClear;

    }

    public void putSingleInstance(RootFragment fragment) {
        ArrayList<RootFragment> frags = new ArrayList<>();
        frags.add(fragment);
        stackList.add(frags);
    }

    public void onBackPressed() {
        int i = stackList.size() - 1;
        if (i >= 0) {
            ArrayList<RootFragment> lastStack = stackList.get(i);
            if (lastStack != null && (!lastStack.isEmpty())) {
                lastStack.remove(lastStack.size() - 1);
                if (lastStack.isEmpty()) {
                    stackList.remove(lastStack);
                }
            } else {
                stackList.remove(lastStack);
            }
        } else {
            stackList.clear();
        }
    }

    protected void setCloseFragmentListener(CloseFragment listener) {
        this.listener = listener;
    }

    protected RootFragment[] getLast() {
        RootFragment[] fagArr = new RootFragment[2];
        boolean hasFirst = false;
        for (int x = stackList.size() - 1; x >= 0; x--) {
            ArrayList<RootFragment> list = stackList.get(x);
            if (list != null && (!list.isEmpty())) {
                if (hasFirst) {
                    fagArr[1] = list.get(list.size() - 1);
                    break;
                } else {
                    hasFirst = true;
                    fagArr[0] = list.get(list.size() - 1);
                    if (list.size() > 1) {
                        fagArr[1] = list.get(list.size() - 2);
                        break;
                    }
                }
            }
        }
        return fagArr;
    }

    public Fragment getCurrentFragment() {
        ArrayList<RootFragment> lastStack = stackList.get(stackList.size() - 1);
        if (lastStack == null || lastStack.isEmpty()) {
            stackList.remove(lastStack);
            return getCurrentFragment();
        } else {
            return lastStack.get(lastStack.size() - 1);
        }
    }

    public Fragment getAndRemoveLastFragment() {
        ArrayList<RootFragment> lastStack = stackList.get(stackList.size() - 1);
        if (lastStack == null || lastStack.isEmpty()) {
            stackList.remove(lastStack);
            return getAndRemoveLastFragment();
        } else {
            Fragment removedFragment = lastStack.remove(lastStack.size() - 1);
            if (lastStack.isEmpty()) {
                stackList.remove(lastStack);
            }
            return removedFragment;
        }
    }

    public boolean isLast() {
        if (stackList.size() == 1 && stackList.get(0).size() == 1) {
            return true;
        } else {
            return false;
        }
    }
}
