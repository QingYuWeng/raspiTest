package com.example.yuwenqing.babycare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class BlinkService extends Service {

    Socket socket = null;
    private int i=1;
    private String recv_buff="";
    private myBinder mBinder=new myBinder();

    class myBinder extends Binder {
        public String showMessage()
        {
            if(recv_buff.startsWith("blink")&&i==1)
            {
                Intent intent=new Intent(BlinkService.this,VideoMonitor.class);
                PendingIntent pi=PendingIntent.getActivity(BlinkService.this,0,intent,0);
                NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Notification notification=new NotificationCompat.Builder(BlinkService.this)
                        .setContentTitle("警报")
                        .setContentText("您的孩子可能醒了！！")
                        .setSmallIcon(R.drawable.icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.icon))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .build();
                manager.notify(3,notification);
                i=0;
            }
            if(recv_buff.startsWith("noblink"))
            {
                i=1;
            }
            Log.i("BL",recv_buff);
            return recv_buff;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(getResources().getString(R.string.host), 7654);
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

    public BlinkService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
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
