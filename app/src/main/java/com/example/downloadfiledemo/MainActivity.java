package com.example.downloadfiledemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.downloadfiledemo.Service.DownloadService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Button btn_download, btn_cancel, btn_pause,btn_delete;
    private DownloadService.DownloadBinder binder;

    //创建serviceConnection实例，这个实例会当调用bindservice的时候回调，并调用里面的
    //onServiceConnected方法，在这个方法里面可以拿到service返回的binder对象，这样就可以使用服务里面的方法了
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DownloadService.DownloadBinder) service;
            Log.d(TAG, "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_download = findViewById(R.id.btn_download);
        btn_pause = findViewById(R.id.btn_pause);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_delete=findViewById(R.id.btn_delete);
        btn_download.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

        Intent intent = new Intent(MainActivity.this, DownloadService.class);

        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        //Log.d(TAG, "onCreate: bind");

        //动态获取权限，如果没有权限，就执行requestPermissions方法，去请求所需要的权限，
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        //如果没有绑定服务，点击啥都没用，直接返回
        if (binder == null) {
            return;
        }
        //之后就只点击不同的按钮，执行服务里面不同的方法，这些方法都是在子线程中实现的，而在子线程这个又是通过AsyncTask实现的，
        switch (v.getId()) {
            case R.id.btn_download:
                binder.startDowload("http://10.95.46.192:8080/files/test.pdf");
                break;
            case R.id.btn_pause:
                binder.pauseDownload();
                break;
            case R.id.btn_cancel:
                binder.cancelDownload();
                break;
            case R.id.btn_delete:
                binder.deleteFile();
                break;
            default:break;
        }
    }

    //请求权限时会回调的方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: recept");
                } else {
                    Toast.makeText(MainActivity.this, "你拒绝了权限", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}