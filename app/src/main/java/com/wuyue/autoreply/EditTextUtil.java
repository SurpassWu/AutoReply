package com.wuyue.autoreply;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by wuyue on 2017/5/25.
 */

public class EditTextUtil {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void findEditText4(AccessibilityNodeInfo rootNode) {
//        int count = rootNode.getChildCount();

//        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + count);
//        for (int i = 0; i < count; i++) {
//            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
//            if (nodeInfo == null) {
//                android.util.Log.d("maptrix", "nodeinfo = null");
//                continue;
//            }
        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/agt");
        android.util.Log.d("maptrix", "class=" + rootNode.getClassName());
        android.util.Log.e("maptrix", "ds=" + rootNode.getContentDescription());
        if (list.size() > 0) {
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException mE) {
                    mE.printStackTrace();
                }
            }
        }

//        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean findEditText2(AccessibilityNodeInfo rootNode, String content) {
//        int count = rootNode.getChildCount();

//        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + count);
//        for (int i = 0; i < count; i++) {
//            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
//            if (nodeInfo == null) {
//                android.util.Log.d("maptrix", "nodeinfo = null");
//                continue;
//            }
        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a3b");
//            android.util.Log.d("maptrix", "class=" + nodeInfo.getClassName());
//            android.util.Log.e("maptrix", "ds=" + nodeInfo.getContentDescription());
//            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
        for (AccessibilityNodeInfo item : list) {
            android.util.Log.i("maptrix", "==================");
//                Bundle arguments = new Bundle();
//                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
//                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
//                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
//                        true);
//                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
//                        arguments);
            item.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, content);
            item.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }

//            if (findEditText2(nodeInfo, content)) {
//                return true;
//            }
//        }

//        return false;
        return true;
    }

}
