package com.example.downloadfiledemo;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


//一个异步任务，通过继承AsyncTask来实现，AsyncTask默认会在doInBackground中开启一个子线程，然后就可以在这个函数里面去写下载的逻辑。
public class DownLoadTask extends AsyncTask<String, Integer, Integer> {

    //四种下载的结果状态
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;


    public DownloadListener downloadListener;

    private boolean isCancel = false;
    private boolean isPause = false;

    private int lastProgress=0;

    public DownLoadTask(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }


    //在后台执行具体的下载逻辑
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream in=null;

        //实现断点续传的对象
        RandomAccessFile randomAccessFile=null;

        File file=null;
        try{
            long lengthCurrent=0;
            String downloadUrl=strings[0];

            //获取到文件对象，便于后续去判文件是否存在以及文件的大小是多少
            String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String saveDictory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            //Log.d("TAG", "doInBackground: "+fileName);
            file=new File(saveDictory+fileName);

            if(file.exists()){
                lengthCurrent=file.length();
            }
            long lengthTotal=getContentLength(downloadUrl);
            Log.d("TAG", "doInBackground: "+lengthTotal);
            if (lengthTotal==0){
                return TYPE_FAILED;
            }else if(lengthTotal==lengthCurrent){
                return TYPE_SUCCESS;
            }
            //Log.d("TAG", "doInBackground: "+"!!!");
            //解下来的情况就是通过断点续传的方式，对没下载好的文件继续下载,先创建一个链接
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .addHeader("RANGE","bytes="+lengthCurrent+"-")
                    .url(downloadUrl)
                    .build();
            Response response=client.newCall(request).execute();
            if (response!=null){
                //链接不为空，使用断点续传下载
                in=response.body().byteStream();
                randomAccessFile=new RandomAccessFile(file,"rw");
                //可以跳过已经下载的字节，从对应的位置开始下载
                randomAccessFile.seek(lengthCurrent);
                byte temp[]=new byte[1024];
                int len;
                int total=0;
                while ((len=in.read(temp))!=-1){
                    if (isCancel){
                        return TYPE_CANCELED;
                    }else if (isPause){
                        return TYPE_PAUSED;
                    }else {
                        total+=len;
                        //不断的写
                        randomAccessFile.write(temp,0,len);
                        int currentProgress=(int)((total+lengthCurrent)*100/lengthTotal);
                        publishProgress(currentProgress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }

        }catch (Exception e){
            Log.d("TAG", "doInBackground: "+e.getMessage());
        }

        return TYPE_FAILED;
    }


    //当执行publishProgress时回回调这个函数，在这个函数中去回调监听器的方法，更新progress
    @Override
    protected void onProgressUpdate(Integer... values) {
        int currentProgress=values[0];
        if(currentProgress>lastProgress){
            downloadListener.onProgress(currentProgress);
            lastProgress=currentProgress;
        }
    }

    //当在doInBackground中调用return语句时会回调这个函数，根据返回结果去回调监听器的方法
    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_CANCELED:
                downloadListener.onCanceled();
                break;
            case TYPE_PAUSED:
                downloadListener.onPaused();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            default:break;
        }
    }

    //用来控制下载的过程的两个函数
    public void PauseDownload(){
        isPause=true;
    }
    public void CancelDownload(){
        isCancel=true;
    }

    //这个函数可以通过一个okhttp链接，得到整个文件的大小
    public long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        //如果能够连接成功，这区获取文件的大小并返回。
        if (response != null && response.isSuccessful()) {
            Log.d("TAG", "getContentLength: ");
            response.close();
            return response.body().contentLength();
        }
        return 0;
    }
}
