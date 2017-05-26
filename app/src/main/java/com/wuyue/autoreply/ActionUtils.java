package com.wuyue.autoreply;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by wuyue on 2017/5/25.
 */

public class ActionUtils {
    //确定点击
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void confirmButton(AccessibilityNodeInfo nodeConfirm) {
        List<AccessibilityNodeInfo> nodeConfirmList = nodeConfirm.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gd");
        for (AccessibilityNodeInfo info : nodeConfirmList) {
            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 邀请入群标签页选定用户
     *
     * @param nodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void selectUserInLabel(AccessibilityNodeInfo nodeInfo) {

        List<AccessibilityNodeInfo> listUser = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/but");
        for (AccessibilityNodeInfo item : listUser) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException mE) {
                mE.printStackTrace();
            }
            item.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        ActionUtils.confirmButton(nodeInfo);
        nodeInfo.recycle();
    }

    /**
     * 进入群信息页面，点击加号添加成员
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void addGroup(AccessibilityNodeInfo nodeInfo) {
        List<AccessibilityNodeInfo> listId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            listId = nodeInfo.findAccessibilityNodeInfosByText("添加成员");

        }
        if (listId.size() > 0) {
            for (AccessibilityNodeInfo item : listId) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else {
            for (int i = 0; i < nodeInfo.getChild(0).getChildCount(); i++) {
                if (nodeInfo.getChild(0).getChild(i).getClassName().equals("android.widget.ListView")) {
                    AccessibilityNodeInfo node_lsv = nodeInfo.getChild(0).getChild(i);
                    node_lsv.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException mE) {
                mE.printStackTrace();
            }
            listId = nodeInfo.findAccessibilityNodeInfosByText("添加成员");
            for (AccessibilityNodeInfo item : listId) {
                item.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 判断服务是否开启
     * @param mContext
     * @param serviceName
     * @return
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(60);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.contains(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

}
