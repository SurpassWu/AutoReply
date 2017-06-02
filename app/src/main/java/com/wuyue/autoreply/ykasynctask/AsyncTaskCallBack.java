package com.wuyue.autoreply.ykasynctask;

/**
 * 异步任务处理回调接口
 * @author liuhai
 * @date 2015/12/15
 * @time 17:18
 */

public interface AsyncTaskCallBack {
    Object onTaskExecute(int asyncTaskType, Object... asyncTaskParams);
    void onTaskComplete(int asyncThreadType, Object asyncTaskResult);
}
