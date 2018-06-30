package com.example.yuwenqing.babycare;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {

    private String send_buff=null;
    private String recv_buff=null;
    private String interNoise="";
    private String state="";

    private Handler handler = null;
    private Handler handler1=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x01:
                    showNoise.setText(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

    private TextView tv_recv;

    Socket socket = null;

    Button openAir=null;
    Button downAir=null;
    Button shake=null;
    Button stop=null;
    Button monitor=null;
    Button openLight=null;
    Button downLight=null;
    TextView temperature=null;
    TextView humidity=null;
    TextView showNoise=null;

    private MyService.myBinder myBinder;

    private ServiceConnection connection=new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder =(MyService.myBinder)iBinder;
            interNoise=myBinder.showMessage();
            state=interNoise;
            showNoise.setText(interNoise);
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    while (true)
                    {
                        interNoise=myBinder.showMessage();
                        if(!interNoise.equals(state))
                        {
                            state=interNoise;
                            Message msg=handler1.obtainMessage();
                            msg.what=0x01;
                            msg.obj=interNoise;
                            handler1.sendMessage(msg);
                        }

                    }
                }
            }).start();


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openAir=(Button)findViewById(R.id.airControl_open);
        downAir=(Button)findViewById(R.id.airControl_down);

        shake=(Button)findViewById(R.id.shakeBed);
        stop=(Button)findViewById(R.id.no_shakeBed);

        monitor=(Button)findViewById(R.id.vedio);

        openLight=(Button)findViewById(R.id.openLight);
        downLight=(Button)findViewById(R.id.downLight);

        tv_recv=(TextView)findViewById(R.id.tv_recv);

        temperature=(TextView)findViewById(R.id.tem);
        humidity=(TextView)findViewById(R.id.hum);

        showNoise=(TextView)findViewById(R.id.noise);

        handler = new Handler();

        Intent bindIntent=new Intent(this,MyService.class);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                   Thread.sleep(100);
                    socket = new Socket("172.24.53.2" , 7654);
                    if (socket!=null) {
                        while (true) {      //循环进行收发
                            recv();
                            while (send_buff==null) {
                            }
                            OutputStream outputStream=socket.getOutputStream();
                            outputStream.write(send_buff.getBytes());
                            outputStream.flush();
                            Log.i("Socket_Information:", "heve send:" +send_buff);
                            send_buff=null;
                        }
                    }
                    else
                        System.out.println("socket is null");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        openAir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"openAir",Toast.LENGTH_SHORT).show();
            }
        });

        downAir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"downAir",Toast.LENGTH_SHORT).show();
            }
        });

        shake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="shake";
                Toast.makeText(MainActivity.this,"shake bed",Toast.LENGTH_SHORT).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="stop";
                Toast.makeText(MainActivity.this,"stop shake bed",Toast.LENGTH_SHORT).show();
            }
        });

        monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,VideoMonitor.class);
                startActivity(intent);
            }
        });

        openLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="open";
                Toast.makeText(MainActivity.this,"open light",Toast.LENGTH_SHORT).show();
            }
        });

        downLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="down";
                Toast.makeText(MainActivity.this,"close light",Toast.LENGTH_SHORT).show();
            }
        });
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
        //将受到的数据显示在TextView上
        if (recv_buff!=null){
            handler.post(runnableUi);

        }
    }

    //不能在子线程中刷新UI，应为textView是主线程建立的
    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            if(recv_buff.startsWith("Tem_Hum:"))
            {
                temperature.setText((recv_buff.split(" ")[0]).split(":")[1]);
                humidity.setText(recv_buff.split(" ")[1]);
            }
            else
                tv_recv.append("\n"+recv_buff);
        }
    };
}
