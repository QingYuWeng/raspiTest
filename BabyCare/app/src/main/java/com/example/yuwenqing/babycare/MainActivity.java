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
    private String WHstate="";
    private String Bed_state="";
    //new
    private String Baby_state="";

    private TextView tv_recv;

    Socket socket = null;

    Button smallFan=null;
    Button stopFan=null;
    Button medioFan=null;
    Button largeFan=null;

    Button shake=null;
    Button stop=null;

    Button monitor=null;

    Button openLight=null;
    Button downLight=null;

    TextView temperature=null;
    TextView humidity=null;

    TextView showNoise=null;
    TextView babyState=null;
    TextView bedState=null;

    private MyService.myBinder myBinder;
    private WHserver.myBinder WHBinder;
    private DumpService.myBinder DumpBinder;
    //new
    private BlinkService.myBinder BlinkBinder;

    //消息处理
    private Handler handler = null;

    private Handler handler1=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x01:
                    showNoise.setText(msg.obj.toString());
                    break;
                case 0x11:
                    bedState.setText(msg.obj.toString());
                    break;
                case 0x12:
                    babyState.setText(msg.obj.toString());
                    break;
                case 0x10:
                    temperature.setText((msg.obj.toString().split(" ")[0]).split(":")[1]);
                    humidity.setText(msg.obj.toString().split(" ")[1]);
                    break;
                default:
                    break;
            }
        }
    };



    //温湿度服务绑定
    private ServiceConnection WHconnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            WHBinder=(WHserver.myBinder)iBinder;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                    {
                        WHstate=WHBinder.showMessage();
                        if(!WHstate.equals("")) {
                            Message msg=handler1.obtainMessage();
                            msg.what=0x10;
                            msg.obj=WHstate;
                            handler1.sendMessage(msg);
                            try {
                                Thread.sleep(4500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    //婴儿状态
    private ServiceConnection BlinkConnection=new ServiceConnection() {
        private int state=2;
        private int flag=0;
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BlinkBinder=(BlinkService.myBinder)iBinder;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                    {
                        Baby_state=BlinkBinder.showMessage();
                        if(Baby_state.startsWith("noblink"))
                            flag=0;
                        if(Baby_state.startsWith("blink"))
                            flag=1;
                        if(state!=flag)
                        {
                            state=flag;
                            Message msg = handler1.obtainMessage();
                            msg.what = 0x12;
                            msg.obj = Baby_state;
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

    //声音监测服务绑定
    private ServiceConnection connection=new ServiceConnection() {
        private int state=2;
        private int flag=0;
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            myBinder =(MyService.myBinder)iBinder;

            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    while (true)
                    {
                        interNoise=myBinder.showMessage();
                        if(interNoise.startsWith("Normal"))
                            flag=0;
                        if(interNoise.startsWith("Abnormal"))
                            flag=1;
                        if(state!=flag)
                        {
                            state=flag;
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


    //倾倒监测服务
    private ServiceConnection DumpConnection=new ServiceConnection() {
        private int state=2;
        private int flag=0;
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            DumpBinder =(DumpService.myBinder)iBinder;

            new Thread(new Runnable() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    while (true)
                    {
                        Bed_state=DumpBinder.showMessage();
                        if(Bed_state.startsWith("Down"))
                            flag=0;
                        if(Bed_state.startsWith("NoDown"))
                            flag=1;
                        if(state!=flag)
                        {
                            state=flag;
                            Message msg=handler1.obtainMessage();
                            msg.what=0x11;
                            msg.obj=Bed_state;
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

        smallFan=(Button)findViewById(R.id.smallFan);
        medioFan=(Button)findViewById(R.id.medioFan);
        largeFan=(Button)findViewById(R.id.LargeFan);
        stopFan=(Button)findViewById(R.id.stopFan);


        shake=(Button)findViewById(R.id.shakeBed);
        stop=(Button)findViewById(R.id.no_shakeBed);

        monitor=(Button)findViewById(R.id.vedio);

        openLight=(Button)findViewById(R.id.openLight);
        downLight=(Button)findViewById(R.id.downLight);

        tv_recv=(TextView)findViewById(R.id.tv_recv);

        temperature=(TextView)findViewById(R.id.tem);
        humidity=(TextView)findViewById(R.id.hum);

        showNoise=(TextView)findViewById(R.id.noise);

        babyState=(TextView)findViewById(R.id.babyState);
        bedState=(TextView)findViewById(R.id.bedState);

        handler = new Handler();

        //声音服务绑定
        Intent bindIntent=new Intent(this,MyService.class);
        Intent WHIntent=new Intent(this,WHserver.class);
        Intent dumpIntent=new Intent(this,DumpService.class);
        Intent blinkIntent=new Intent(this,BlinkService.class);

        //温湿度服务绑定
        //开启此服务则可以进行眨眼检测
       //bindService(blinkIntent,BlinkConnection,BIND_AUTO_CREATE);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
        bindService(WHIntent,WHconnection,BIND_AUTO_CREATE);
        bindService(dumpIntent,DumpConnection,BIND_AUTO_CREATE);



        //指令模块
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                   Thread.sleep(100);
                    socket = new Socket(getResources().getString(R.string.host), 7654);
                    if (socket!=null) {
                        while (true) {      //循环进行收发
                            recv();
                            while (send_buff==null) {
                            }
                            OutputStream outputStream=socket.getOutputStream();
                            outputStream.write(send_buff.getBytes());
                            outputStream.flush();
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

        smallFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="s";
                Toast.makeText(MainActivity.this,send_buff,Toast.LENGTH_SHORT).show();
            }
        });

        medioFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="m";
                Toast.makeText(MainActivity.this,send_buff,Toast.LENGTH_SHORT).show();
            }
        });

        largeFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="l";
                Toast.makeText(MainActivity.this,send_buff,Toast.LENGTH_SHORT).show();
            }
        });

        stopFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_buff="t";
                Toast.makeText(MainActivity.this,send_buff,Toast.LENGTH_SHORT).show();
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
            tv_recv.append("\n"+recv_buff);
        }
    };
}
