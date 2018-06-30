package com.example.yuwenqing.babycare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MyService extends Service {


    Socket socket = null;
    private String recv_buff="";
    private myBinder mBinder=new myBinder();

    class myBinder extends Binder{
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public String showMessage()
        {
            Log.i("Service","recv"+recv_buff);
            return recv_buff;
        }
    }

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Service","create");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("172.24.53.2", 7654);
                    if (socket!=null) {
                        while (true) {      //循环进行收发
                            recv();
                        }
                    }
                    else
                        System.out.println("socket is null");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Service","start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void recv() {

        //单开一个线程循环接收来自服务器端的消息
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputStream!=null){
            try {
                byte[] buffer = new byte[1024];
                int count = inputStream.read(buffer);//count是传输的字节数
                recv_buff = new String(buffer);//socket通信传输的是byte类型，需要转为String类型

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
