package com.wuyue.autoreply.action;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by wuyue on 2017/6/2.
 */

public abstract class BaseAction {
    private BaseAction mNextAction;

    private boolean mDone;

    public BaseAction getNextAction() {
        return mNextAction;
    }

    public void setNextAction(BaseAction nextAction) {
        this.mNextAction = nextAction;
    }

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean done) {
        this.mDone = done;
    }

    abstract void doAction(AccessibilityNodeInfo nodeInfo);

    abstract void otherCondition(boolean condition);
}
