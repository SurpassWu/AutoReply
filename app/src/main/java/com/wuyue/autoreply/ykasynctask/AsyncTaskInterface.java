package com.wuyue.autoreply.ykasynctask;

/**
 * 异步任务接口
 * @author liuhai
 * @date 2015/12/15
 * @time 15:17
 */
public class AsyncTaskInterface {
    
    private volatile static AsyncTaskInterface sAsyncTaskInterface;

    private AsyncTaskDispatch mAsyncTaskDispatch;

    public static synchronized AsyncTaskInterface getInstance() {

        if (null == sAsyncTaskInterface){
            synchronized (AsyncTaskInterface.class){

                if (null == sAsyncTaskInterface){
                    sAsyncTaskInterface = new AsyncTaskInterface();
                }
            }
        }

        return sAsyncTaskInterface;
    }

    private AsyncTaskInterface() {
        mAsyncTaskDispatch = new AsyncTaskDispatch();
    }

    public void addAsyncTask(int asyncTaskType, AsyncTaskCallBack asyncTaskCallBack, Object... asyncTaskParams){
        mAsyncTaskDispatch.addAsyncTask(asyncTaskType, asyncTaskCallBack, asyncTaskParams);
    }

    public void removeAsyncTask(AsyncTaskCallBack asyncTaskCallBack){
        mAsyncTaskDispatch.removeAsyncTask(asyncTaskCallBack);
    }
}
