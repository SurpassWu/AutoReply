package com.wuyue.autoreply.action;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by wuyue on 2017/6/2.
 */

public class AcceptAction extends BaseAction {

    private boolean mHasAddQun;

    @Override
   public void doAction(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> list;
        list = nodeInfo.findAccessibilityNodeInfosByText("接受");
        if(null != list && list.size() > 0) {
            for (AccessibilityNodeInfo n : list) {
                if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (!mHasAddQun) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException mE) {
                            mE.printStackTrace();
                        }
                    }
                }
            }
        }else{
            getNextAction().doAction(nodeInfo);
        }
    }

    @Override
    public void otherCondition(boolean condition) {
        this.mHasAddQun = condition;
    }
}
