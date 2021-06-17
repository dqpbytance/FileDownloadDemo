package com.example.downloadfiledemo;


//监听回调接口，主要是用于下载过程中监听下载任务中发生的一系列事件，根据事件内容回调对应的方法
public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
