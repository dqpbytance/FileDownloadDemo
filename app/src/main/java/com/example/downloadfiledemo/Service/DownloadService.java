package com.example.downloadfiledemo.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.downloadfiledemo.DownLoadTask;
import com.example.downloadfiledemo.DownloadListener;
import com.example.downloadfiledemo.MainActivity;
import com.example.downloadfiledemo.R;

import java.io.File;

public class DownloadService extends Service {

    public DownLoadTask downLoadTask;
    private String downbloadurl;

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificaionManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            downLoadTask = null;
            //下载成功时将前台通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificaionManager().notify(1, getNotification("Download success...", -1));
            Toast.makeText(DownloadService.this, "尽情享受吧", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downLoadTask = null;
            //下载失败将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificaionManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownloadService.this, "下载失败了", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downLoadTask = null;
            Toast.makeText(DownloadService.this, "暂停下载", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downLoadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "取消下载", Toast.LENGTH_SHORT).show();
        }
    };

    //getNotification方法构建通知，我读代码观察到的是，每当progress变化都会新建一个notification，唯一的区别可能就是这个progress
    //设置setProgress用于设置进度。第一个参数是最大进度，第二个是当前进度。
    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        String channelId = createNotificationChannel("my_channel_ID", "my_channel_NAME", NotificationManager.IMPORTANCE_HIGH);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,channelId)
                .setContentIntent(pi)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_background);
        if (progress >= 0) {
            //当progress大于0时需要显示下载进度
            notification.setContentText(progress + "%");
            notification.setProgress(100, progress, false);
        }
        return notification.build();
    }

    //返回一个通知管理器
    private NotificationManagerCompat getNotificaionManager() {
        return NotificationManagerCompat.from(this);
    }

    private DownloadBinder mBinder = new DownloadBinder();


    //返回控制这个服务的binder
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //创建的DownloadBinder继承自Binder提供了startDowload、pauseDownload和cancelDownload方法。这几个方法是服务提供给外界的接口。
    public class DownloadBinder extends Binder {

        public void startDowload(String url) {
            //如果任务为空，这创建一个任务并且执行，
            if (downLoadTask == null) {
                downbloadurl = url;
                downLoadTask = new DownLoadTask(listener);
                downLoadTask.execute(downbloadurl);
                startForeground(1, getNotification("Downloading", 0));
                Toast.makeText(DownloadService.this, "开始下载了～请稍等哦", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            //不为空点击才会有用。
            if (downLoadTask != null) {
                downLoadTask.PauseDownload();
            }
        }

        public void cancelDownload() {
            if (downLoadTask != null) {
                downLoadTask.CancelDownload();
                if(downbloadurl!=null){
                    String fileName = downbloadurl.substring(downbloadurl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                        Log.d("TAG", "cancelDownload: 文件删除");
                    }
                    downbloadurl=null;
                }
            }
//            else if (downbloadurl != null) {
//                //取消下载时将文件删除并将通知取消
//                String fileName = downbloadurl.substring(downbloadurl.lastIndexOf("/"));
//                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
//                File file = new File(directory + fileName);
//                if (file.exists()) {
//                    file.delete();
//                }
//                //将通知取消
//                getNotificaionManager().cancel(1);
//                stopForeground(true);
////                Toast.makeText(DownloadService.this, "取消了...", Toast.LENGTH_SHORT).show();
//            }
        }

    }

    //android8.0之后都需要建立一个通道，没有通道没办法创建通知。但其实这个ID也是自己设置的
    private String createNotificationChannel(String channelID, String channelNAME, int level) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            manager.createNotificationChannel(channel);
            //创建好后返回这个自己定的ID
            return channelID;
        } else {
            return null;
        }
    }
}