package com.wuyue.autoreply.action;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by wuyue on 2017/6/2.
 */

public class FinishAction extends BaseAction {

    private boolean mHasAddQun;

    private AutoActionImpl mAutoActionImpl;

    public FinishAction(AutoActionImpl autoActionImpl) {
        this.mAutoActionImpl = autoActionImpl;
    }

    @Override
    public void doAction(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> list;
        list = nodeInfo.findAccessibilityNodeInfosByText("完成");
        if (null != list && list.size() > 0) {
            mAutoActionImpl.doAddlabel();

            for (AccessibilityNodeInfo n : list) {
                if (n.getClassName().equals("android.widget.TextView") && n.isEnabled()) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    @Override
    public void otherCondition(boolean condition) {
        this.mHasAddQun = condition;
    }
}
