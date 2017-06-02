package com.wuyue.autoreply.ykasynctask;


import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步任务调度。
 * @author liuhai
 * @date 2015/12/15
 * @time 15:17
 */

public class AsyncTaskDispatch {

    private static final int KAsyncTaskCount = 5;

    private ExecutorService mAsyncTaskExecutorService;

    private AsyncTaskHandler mAsyncTaskHandler;

    private ArrayList<AsyncTaskItem> mAsyncTaskList;

    public AsyncTaskDispatch() {
        mAsyncTaskList = new ArrayList<>();
        mAsyncTaskExecutorService = Executors.newFixedThreadPool(KAsyncTaskCount);
        mAsyncTaskHandler = new AsyncTaskHandler(this);
    }

    private static class AsyncTaskHandler extends Handler {

        private WeakReference<AsyncTaskDispatch> asyncTaskPrivateWeakReference;

        public AsyncTaskHandler(AsyncTaskDispatch asyncTaskPrivate){
            this.asyncTaskPrivateWeakReference = new WeakReference<>(asyncTaskPrivate);
        }
        @Override
        public void handleMessage(Message msg) {
            int asyncTaskType = msg.what;
            Object asyncTaskResult = msg.obj;
            AsyncTaskDispatch asyncTaskDishPatch = asyncTaskPrivateWeakReference.get();
            if(asyncTaskDishPatch != null){
                ArrayList<AsyncTaskItem> asyncTaskList = asyncTaskDishPatch.mAsyncTaskList;
                ArrayList<AsyncTaskItem> removeAsyncTaskList = new ArrayList<>();
                for(AsyncTaskItem asyncTaskItem:asyncTaskList){
                    if(asyncTaskItem.getAsyncTaskType() == asyncTaskType){
                        removeAsyncTaskList.add(asyncTaskItem);
                    }
                }
                for(AsyncTaskItem removeAsyncTaskItem:removeAsyncTaskList){
                    removeAsyncTaskItem.asyncTaskComplete(asyncTaskType,asyncTaskResult);
                    asyncTaskDishPatch.removeAsyncTaskItem(removeAsyncTaskItem);
                }
            }
            super.handleMessage(msg);
        }
    }

    public void addAsyncTask(int asyncTaskType, AsyncTaskCallBack asyncTaskCallBack, Object... asyncTaskParams){
        synchronized (mAsyncTaskList) {
            AsyncTaskItem asyncTaskItem = new AsyncTaskItem(mAsyncTaskHandler);
            asyncTaskItem.setAsyncTaskType(asyncTaskType);
            asyncTaskItem.setAsyncTaskCallBack(asyncTaskCallBack);
            asyncTaskItem.setAsyncTaskParams(asyncTaskParams);
            mAsyncTaskList.add(asyncTaskItem);
            mAsyncTaskExecutorService.execute(asyncTaskItem.getAsyncTaskRunnable());
        }
    }

    private void removeAsyncTaskItem(AsyncTaskItem asyncTaskItem){
        synchronized (mAsyncTaskList) {
            asyncTaskItem.stopAsyncTask();
            mAsyncTaskList.remove(asyncTaskItem);
        }
    }

    public void removeAsyncTask(AsyncTaskCallBack mAsyncThreadTask){
        synchronized (mAsyncTaskList) {
            ArrayList<AsyncTaskItem> removeAsyncTaskList = new ArrayList<>();
            for(AsyncTaskItem asyncTaskItem:mAsyncTaskList){
                if (asyncTaskItem.getAsyncTaskCallBack() == mAsyncThreadTask){
                    removeAsyncTaskList.add(asyncTaskItem);
                }
            }
            for(AsyncTaskItem removeAsyncTaskItem:removeAsyncTaskList){
                removeAsyncTaskItem.stopAsyncTask();
                mAsyncTaskList.remove(removeAsyncTaskItem);
            }
        }
    }

}
