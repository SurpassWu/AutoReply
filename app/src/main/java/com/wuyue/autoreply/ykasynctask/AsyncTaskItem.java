package com.wuyue.autoreply.ykasynctask;

import android.os.Handler;
import android.os.Message;

/**
 * 异步任务类
 * @author liuhai
 * @date 2015/12/16
 * @time 10:42
 */

public class AsyncTaskItem {
    private int mAsyncTaskType;
    private AsyncTaskCallBack mAsyncTaskCallBack;
    private Object[] mAsyncTaskParams;
    private boolean mIsRun;
    private Handler mAsyncTaskHandler;

    public AsyncTaskItem(Handler handler){
        this.mAsyncTaskHandler = handler;
    }

    public int getAsyncTaskType() {
        return mAsyncTaskType;
    }

    public void setAsyncTaskType(int mAsyncTaskType) {
        this.mAsyncTaskType = mAsyncTaskType;
    }

    public AsyncTaskCallBack getAsyncTaskCallBack() {
        return mAsyncTaskCallBack;
    }

    public void setAsyncTaskCallBack(AsyncTaskCallBack mAsyncTaskCallBack) {
        this.mAsyncTaskCallBack = mAsyncTaskCallBack;
    }

    public Object[] getAsyncTaskParams() {
        return mAsyncTaskParams;
    }

    public void setAsyncTaskParams(Object... asyncTaskParams) {
        mAsyncTaskParams = asyncTaskParams;
    }

    public Runnable getAsyncTaskRunnable(){
        mIsRun = true;
        return new Runnable() {
            @Override
            public void run() {
                if(mIsRun){
                    Object asyncTaskResult = mAsyncTaskCallBack.onTaskExecute(mAsyncTaskType, mAsyncTaskParams);
                    Message message = mAsyncTaskHandler.obtainMessage();
                    message.what = mAsyncTaskType;
                    if(null != asyncTaskResult){
                        message.obj = asyncTaskResult;
                    }
                    mAsyncTaskHandler.sendMessage(message);
                }
            }
        };
    }

    public void stopAsyncTask() {
        mIsRun = false;
    }

    public void asyncTaskComplete(int asyncThreadType, Object asyncTaskResult){
        mAsyncTaskCallBack.onTaskComplete(asyncThreadType,asyncTaskResult);
    }
}
