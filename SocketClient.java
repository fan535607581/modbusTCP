package cn.roger.socket;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.View;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.io.*;
import java.net.*;


@DesignerComponent(version = 1,
    description = "made in fanhaojie",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")

@SimpleObject(external = true)

public class SocketClient extends AndroidNonvisibleComponent {
    Socket socket = null;
    OutputStream ou = null;
    String buffer = "";
    String geted1;
    MyThread mt;
    final int CONNECT = 100001;
    final int SENDMESSAGE = 100002;
    final int CLOSE = 100003;
    public SocketClient(ComponentContainer container) {super(container.$form()); }
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {GetMessage(msg.obj.toString()); }
    };
    @SimpleFunction(description = "start")
    public void closeConnect(){
        if(socket != null){
            mt = new MyThread(CLOSE);
            mt.start();
        }else{  GetMessage("连接未创建！"); }
    }
    @SimpleFunction(description = "start")
    public void sendMessage(String s)
    {  
	 int k = s.length()/3;
        if(socket != null){
            mt = new MyThread(SENDMESSAGE);
	    for(int j = 0; j<k ;j++)
	    {
		   mt.setText(Integer.parseInt(s.substring(j*3,(j+1)*3)), j , k );
	    }
            mt.start();//启动发送
        }else{ GetMessage("连接未创建！");}
    }
    @SimpleFunction(description = "start")
    public void connect(String ip , int port){
        if(socket == null){
            mt = new MyThread(CONNECT);
            mt.setDK(port);
            mt.setIP(ip);
            mt.start();
        }else{ GetMessage("连接创建失败！"); }
    }

    @SimpleEvent
    public void GetMessage(String s){
        EventDispatcher.dispatchEvent(this, "GetMessage", s);
    }
    class MyThread extends Thread {
 
        public String IP;
        public int DK;
        public int js;
        public int[]i=new int[1024];
        Message message_2;
        Message msg;
        public int flag;
        public MyThread(int flag) { this.flag = flag; }
        public void setText(int s , int b , int k){ i[b] = s;  js = k; }
        public void setIP(String ip){ IP = ip; }
        public void setDK(int port){ DK = port;}
	    
        @Override
        public void run() 
	{
            switch(flag){
                case CONNECT:
                    try {
                        socket = new Socket();
                        msg = myHandler.obtainMessage();
                        msg.obj = "开始连接";
                        myHandler.sendMessage(msg);
			    
			socket.connect(new InetSocketAddress(IP, DK), 1000);
			ou = socket.getOutputStream();
			    
                        msg = myHandler.obtainMessage();
                        msg.obj = "连接成功";
                        myHandler.sendMessage(msg);  
			   
                    } catch (SocketTimeoutException aa) {
                        msg = myHandler.obtainMessage();
                        msg.obj = "连接超时";
                        myHandler.sendMessage(msg);
                        socket = null;
                    } catch (IOException e) {
                        msg = myHandler.obtainMessage();
                        msg.obj = "连接错误";
                        myHandler.sendMessage(msg);
                        socket = null;
                    }
                break;
                case SENDMESSAGE:
                    try {
			ou.write(0);
                        for(int j = 0; j<js ;j++)ou.write(i[j]);
                        msg = myHandler.obtainMessage();
                        msg.obj = "发送完毕";
                        myHandler.sendMessage(msg);
			    
			try {	
			     int msy = 0;  byte[] b = new byte[255];	int k = 0;
			     msy = socket.getInputStream().read(b);
			     if( msy >= 0)	
				for(int j = 0; j<(b[5]+6) ; j++)
				{
					message_2 = myHandler.obtainMessage();
					message_2.obj = b[j]&0xff;
					myHandler.sendMessage(message_2);
				}
			     }catch (IOException e) {
				msg = myHandler.obtainMessage();
				msg.obj = "接收错误";
				myHandler.sendMessage(msg);}
			    
                    }catch (IOException e) {
                        msg = myHandler.obtainMessage();
                        msg.obj = "发送错误";
                        myHandler.sendMessage(msg);	
                    }
                break;
                case CLOSE:
                    try {
                        ou.close();
                        socket.close();
                        socket = null;
                        msg = myHandler.obtainMessage();
                        msg.obj = "关闭";
                        myHandler.sendMessage(msg);
                    }catch (IOException e) {
                        msg = myHandler.obtainMessage();
                        msg.obj = "未知错误";
                        myHandler.sendMessage(msg);
                    }
                break;
            }
        }
    }
}
