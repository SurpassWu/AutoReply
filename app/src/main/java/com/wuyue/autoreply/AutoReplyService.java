package com.wuyue.autoreply;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.List;

public class AutoReplyService extends AccessibilityService {
    private static final int SEND_BUTTON = 0;

    private static final int ACCEPT_BUTTON = 1;

    private static final int FINISH_TEXTVIEW = 3;

    private static final int SEND_MESSAGE_BUTTON = 4;

    private static final String USER_NAME = "user_name";

    private final static String MM_PNAME = "com.tencent.mm";

    /**
     * 一个标签下的好友数量
     */
    private final static String NUMBERS_IN_LABEL = "numbers_in_label";

    /**
     * 添加标签
     */
    private final static String LABEL_NAME = "label_name";

    boolean hasAction = false;

    boolean locked = false;

    boolean background = false;

    private String name;

    private String scontent;

    AccessibilityNodeInfo itemNodeinfo;

    private KeyguardManager.KeyguardLock kl;

    private Handler handler = new Handler();

    int statusSend;

    private boolean mHasBack;

    private boolean mAddQun;

    private boolean mHasSearch;

    //是否添加过标签
    private boolean mHasAddLabel;

    /**
     * 没有加好友过程，只是回复聊天
     */
    private boolean mOnlyTalk;

    private SharePreHelper mSharePreHelper;

    /**
     * 必须重写的方法，响应各种事件。
     *
     * @param event
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        SharePreHelper sharePreHelper = SharePreHelper.getInstance(this);
        int eventType = event.getEventType();
        if (eventType != 2048) {
            android.util.Log.i("maptrix", "get event = " + eventType);
        }
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
                android.util.Log.i("maptrix", "get notification event");
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (!TextUtils.isEmpty(content)) {
                            if (isScreenLocked()) {
                                locked = true;
                                wakeAndUnlock();
                                android.util.Log.d("maptrix", "the screen is locked");
                                if (isAppForeground(MM_PNAME)) {
                                    background = false;
                                    android.util.Log.d("maptrix", "is mm in foreground");
                                    sendNotifacationReply(event);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendNotifacationReply(event);
                                            if (fill()) {
                                                send();
                                            }
                                        }
                                    }, 1000);
                                } else {
                                    background = true;
                                    android.util.Log.d("maptrix", "is mm in background");
                                    sendNotifacationReply(event);
                                }
                            } else {
                                locked = false;
                                android.util.Log.d("maptrix", "the screen is unlocked");
                                if (isAppForeground(MM_PNAME)) {
                                    background = false;
                                    android.util.Log.d("maptrix", "is mm in foreground");
                                    sendNotifacationReply(event);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fill()) {
                                                send();
                                            }
                                        }
                                    }, 1000);
                                } else {
                                    background = true;
                                    android.util.Log.d("maptrix", "is mm in background");
                                    sendNotifacationReply(event);
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (mHasSearch) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException mE) {
                        mE.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException mE) {
                        mE.printStackTrace();
                    }
                }

                if (event.getPackageName().equals(MM_PNAME)) {
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    List<AccessibilityNodeInfo> list;
                    list = nodeInfo.findAccessibilityNodeInfosByText("接受");

                    if (list == null || list.size() == 0) {
                        list = nodeInfo.findAccessibilityNodeInfosByText("完成");
                    }

                    if (list == null || list.size() == 0) {
                        list = nodeInfo.findAccessibilityNodeInfosByText("发消息");
                        statusSend = SEND_MESSAGE_BUTTON;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException mE) {
                            mE.printStackTrace();
                        }
                    }

                    if (list == null || list.size() == 0) {
                        //点击邀请
                        list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/abz");
                        if (list.size() > 0) {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException mE) {
                                mE.printStackTrace();
                            }
                            for (AccessibilityNodeInfo item : list) {
                                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                            back2Home();
                            release();
                        }
                    }

                    if (null != nodeInfo.getContentDescription()) {
                        if (mHasSearch && nodeInfo.getContentDescription().toString().contains("选择联系人")) {
                            ActionUtils.confirmButton(nodeInfo);
                        }
                    }

                    if (list == null || list.size() == 0) {
                        //添加标签
                        if (!mHasAddLabel) {
                            list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gr");
                            if (list.size() > 0) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException mE) {
                                    mE.printStackTrace();
                                }
                                for (AccessibilityNodeInfo item : list) {
                                    if (sharePreHelper.getPref(NUMBERS_IN_LABEL, 0) < 3) {
                                        sharePreHelper.setPref(NUMBERS_IN_LABEL, sharePreHelper.getPref(NUMBERS_IN_LABEL, 0) + 1);
                                        sharePreHelper.setPref(LABEL_NAME, sharePreHelper.getPref(LABEL_NAME, 0));
                                    } else {
                                        sharePreHelper.setPref(LABEL_NAME, sharePreHelper.getPref(LABEL_NAME, 0) + 1);
                                        sharePreHelper.setPref(NUMBERS_IN_LABEL, 1);
                                    }

                                    android.util.Log.i("maptrix", "==================");
//                                    Bundle arguments = new Bundle();
//                                    arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
//                                            AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
//                                    arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
//                                            true);
//                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
//                                            arguments);
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
//                                    ClipData clip = ClipData.newPlainText("" + sharePreHelper.getPref(LABEL_NAME, 0), "" + sharePreHelper.getPref(LABEL_NAME, 0));
//                                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                                    clipboardManager.setPrimaryClip(clip);
                                    item.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                                    Bundle arguments = new Bundle();
                                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "" + sharePreHelper.getPref(LABEL_NAME, 0));
                                    item.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException mE) {
                                        mE.printStackTrace();
                                    }

                                    AccessibilityNodeInfo nodeInfo2 = getRootInActiveWindow();
                                    List<AccessibilityNodeInfo> list1 = nodeInfo2.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gd");
                                    for (AccessibilityNodeInfo item1 : list1) {
                                        item1.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException mE) {

                                    }
                                    mHasAddLabel = true;

                                }
                                break;
                            }

                        }
                    }


                    if (list != null && list.size() > 0) {
                        send();
                    } else {
                        android.util.Log.d("maptrix", "get type window down event");
                        if (!hasAction) {
                            break;
                        }
                        itemNodeinfo = null;
                        String className = event.getClassName().toString();
                        if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                            mHasBack = false;
                            if (fill()) {
                                mOnlyTalk = true;
                                send();
                            } else {
                                if (itemNodeinfo != null) {
                                    itemNodeinfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (fill()) {
                                                send();
                                            }
                                            back2Home();
                                            release();
                                            hasAction = false;
                                        }
                                    }, 500);
                                    break;
                                }
                            }
                        } else {
                            if (statusSend == SEND_MESSAGE_BUTTON) {
                                if (invitationFillContent()) {
                                    send();
                                }
                            }
                        }
                        //bring2Front();
//                        back2Home();
//                        release();
                        hasAction = false;
                    }
                }


                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:

                if (mOnlyTalk) {
                    back2Home();
                    release();
                    break;
                }

                if (!mHasSearch) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException mE) {
                        mE.printStackTrace();
                    }


//                    if (mHasBack && !mAddQun && !mHasAddLabel) {
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException mE) {
//                            mE.printStackTrace();
//                        }
//                        checkUserInfo();
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException mE) {
//                        mE.printStackTrace();
//                    }
                    AccessibilityNodeInfo nodeInfo1 = getRootInActiveWindow();
                    List<AccessibilityNodeInfo> listLable = nodeInfo1.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cee");
                    if (listLable.size() > 0) {
                        for (AccessibilityNodeInfo info : listLable) {
                            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }

                    if (null != nodeInfo1.getContentDescription()) {
                        if (mAddQun) {
                            clickInfo();
                        }
                        if (nodeInfo1.getContentDescription().toString().contains("聊天信息")) {
                            ActionUtils.addGroup(nodeInfo1);
                        }

                        if (nodeInfo1.getContentDescription().toString().contains("选择联系人")) {
                            if (!mHasSearch) {
                                searchUser();
                            }
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException mE) {
                            mE.printStackTrace();
                        }
                        AccessibilityNodeInfo nodeInfo2 = getRootInActiveWindow();
                        if (nodeInfo2.getContentDescription().toString().contains("" + sharePreHelper.getPref(LABEL_NAME, 0))) {
                            if (mHasSearch) {
                                ActionUtils.selectUserInLabel(nodeInfo2);
                            }
                        }
                    }
                }
                if (mHasAddLabel && !mHasSearch) {
                    if( sharePreHelper.getPref(NUMBERS_IN_LABEL, 0) == 3) {
                        addQun();
                    }else{
                        back2Home();
                        release();
                    }
                }
                break;
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo mAccessibilityServiceInfo = new AccessibilityServiceInfo();
        // 响应事件的类型，这里是全部的响应事件（长按，单击，滑动等）
        mAccessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        // 反馈给用户的类型，这里是语音提示
        mAccessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        setServiceInfo(mAccessibilityServiceInfo);
    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    @SuppressLint("NewApi")
    private void send() {
        int status = 0;
        String widget = "";
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list;
            list = nodeInfo
                    .findAccessibilityNodeInfosByText("发送");
            if (list == null || list.size() == 0) {
                list = nodeInfo
                        .findAccessibilityNodeInfosByText("接受");
                status = ACCEPT_BUTTON;
            }
            if (list == null || list.size() == 0) {
                list = nodeInfo
                        .findAccessibilityNodeInfosByText("完成");
                status = FINISH_TEXTVIEW;
            }

            if (list == null || list.size() == 0) {
                if (!mAddQun) {
                    list = nodeInfo
                            .findAccessibilityNodeInfosByText("发消息");
                    status = SEND_MESSAGE_BUTTON;
                }
            }


            if (list != null && list.size() > 0) {
                if (status == SEND_BUTTON || status == ACCEPT_BUTTON || status == SEND_MESSAGE_BUTTON) {
                    widget = "android.widget.Button";
                } else if (status == FINISH_TEXTVIEW) {
                    widget = "android.widget.TextView";
//                    newName();
                    if (!mHasAddLabel) {
                        addLabel();
                        return;
                    }

//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException mE) {
//                        mE.printStackTrace();
//                    }
                }
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals(widget) && n.isEnabled()) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (!mAddQun) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException mE) {
                                mE.printStackTrace();
                            }
                            if (status == SEND_BUTTON) {
                                backPress();
                                mHasBack = true;
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拉起微信界面
     *
     * @param event
     */
    private void sendNotifacationReply(AccessibilityEvent event) {
        final SharePreHelper sharePreHelper = SharePreHelper.getInstance(this);

        hasAction = true;
        if (event.getParcelableData() != null
                && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event
                    .getParcelableData();
            String content = notification.tickerText.toString();
            if (content.contains(":")) {
                String[] cc = content.split(":");

                name = cc[0].trim();
                scontent = cc[1].trim();
                sharePreHelper.setPref(USER_NAME, name);
                android.util.Log.i("maptrix", "sender name =" + name);
                android.util.Log.i("maptrix", "sender content =" + scontent);
            }


            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private boolean fill() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            return findEditText(rootNode, "正在忙,稍后回复你");
        }
        return false;
    }

    @SuppressLint("NewApi")
    private boolean invitationFillContent() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null && !mAddQun) {
            return EditTextUtil.findEditText2(rootNode, "hello");
        }
        return false;

    }

    @SuppressLint("NewApi")
    private void newName() {
        SharePreHelper helper = SharePreHelper.getInstance(this);
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        String name = "" + System.currentTimeMillis();
        if (rootNode != null) {
            findEditText3(rootNode, name);
            helper.setPref(USER_NAME, name);
        }

    }

    /**
     * 添加标签
     */

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void addLabel() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/caw");
        for (AccessibilityNodeInfo item : list) {
            item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        int count = rootNode.getChildCount();

        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + count);
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                android.util.Log.d("maptrix", "nodeinfo = null");
                continue;
            }

            android.util.Log.d("maptrix", "class=" + nodeInfo.getClassName());
            android.util.Log.e("maptrix", "ds=" + nodeInfo.getContentDescription());
            if (nodeInfo.getContentDescription() != null && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(scontent)) {
                int nindex = nodeInfo.getContentDescription().toString().indexOf(name);
                int cindex = nodeInfo.getContentDescription().toString().indexOf(scontent);
                android.util.Log.e("maptrix", "nindex=" + nindex + " cindex=" + cindex);
                if (nindex != -1) {
                    itemNodeinfo = nodeInfo;
                    android.util.Log.i("maptrix", "find node info");
                }
            }
            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                android.util.Log.i("maptrix", "==================");
//                Bundle arguments = new Bundle();
//                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
//                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
//                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
//                        true);
//                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
//                        arguments);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
//                ClipData clip = ClipData.newPlainText("label123", content);
//                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                clipboardManager.setPrimaryClip(clip);
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, content);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
//                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }

            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean findEditText3(AccessibilityNodeInfo rootNode, String content) {
        int count = rootNode.getChildCount();

        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + count);
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                android.util.Log.d("maptrix", "nodeinfo = null");
                continue;
            }
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cat");
            android.util.Log.d("maptrix", "class=" + nodeInfo.getClassName());
            android.util.Log.e("maptrix", "ds=" + nodeInfo.getContentDescription());
            if (list.size() > 0) {
                for (AccessibilityNodeInfo item : list) {
                    item.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException mE) {
                        mE.printStackTrace();
                    }
                    android.util.Log.i("maptrix", "==================");
//                    Bundle arguments = new Bundle();
//                    arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
//                            AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
//                    arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
//                            true);
//                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
//                            arguments);
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
//                    ClipData clip = ClipData.newPlainText(content, content);
//                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    clipboardManager.setPrimaryClip(clip);
//                    item.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, content);
                    item.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                }


                return true;
            }

            if (findEditText3(nodeInfo, content)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {

    }


    /**
     * 判断指定的应用是否在前台运行
     *
     * @param packageName
     * @return
     */
    private boolean isAppForeground(String packageName) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName)) {
            return true;
        }

        return false;
    }


    /**
     * 将当前应用运行到前台
     */
    private void bring2Front() {
        ActivityManager activtyManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activtyManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTaskInfos) {
            if (this.getPackageName().equals(runningTaskInfo.topActivity.getPackageName())) {
                activtyManager.moveTaskToFront(runningTaskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                return;
            }
        }
    }

    /**
     * 回到系统桌面
     */
    private void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);

        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        mAddQun = false;
        mHasSearch = false;
        mHasAddLabel = false;
        mOnlyTalk = false;
        startActivity(home);
    }


    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    private boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    private void wakeAndUnlock() {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

        //点亮屏幕
        wl.acquire(1000);

        //得到键盘锁管理器对象
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");

        //解锁
        kl.disableKeyguard();

    }

    private void release() {

        if (locked && kl != null) {
            android.util.Log.d("maptrix", "release the lock");
            //得到键盘锁管理器对象
            kl.reenableKeyguard();
            locked = false;
        }
    }

    /**
     * 模拟点击返回
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void backPress() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException mE) {
            mE.printStackTrace();
        }
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);   // 返回

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void addQun() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (mHasBack) {

            Log.i("kaimo", "nodeinfo=" + nodeInfo.getContentDescription());
            List<AccessibilityNodeInfo> listId = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                listId = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aft");

            }
            if (listId != null || listId.size() > 0) {
                for (AccessibilityNodeInfo item : listId) {
                    if (item.equals(listId.get(0))) {
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        mAddQun = true;
                    }
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void clickInfo() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            listId = nodeInfo.findAccessibilityNodeInfosByText("聊天信息");

        }
        if (listId != null || listId.size() > 0) {
            for (AccessibilityNodeInfo item : listId) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void searchUser() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException mE) {
            mE.printStackTrace();
        }
        final SharePreHelper sharePreHelper = SharePreHelper.getInstance(this);
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        EditTextUtil.findEditText4(nodeInfo);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException mE) {
            mE.printStackTrace();
        }
        AccessibilityNodeInfo nodeInfo1 = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listId = nodeInfo1.findAccessibilityNodeInfosByText("" + sharePreHelper.getPref(LABEL_NAME, 0));
        for (AccessibilityNodeInfo item : listId) {
            item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        mHasSearch = true;
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException mE) {
//            mE.printStackTrace();
//        }
//
//        AccessibilityNodeInfo nodeInfo2 = getRootInActiveWindow();
//        List<AccessibilityNodeInfo> list = nodeInfo2.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gd");
//        for (AccessibilityNodeInfo item : list) {
//            item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        }
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException mE) {
//            mE.printStackTrace();
//        }
    }


    /**
     * 顶部搜索
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkUserInfo() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            listId = nodeInfo.findAccessibilityNodeInfosByText("搜索");

        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException mE) {
            mE.printStackTrace();
        }
        if (listId != null || listId.size() > 0) {
            for (AccessibilityNodeInfo item : listId) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }


}
