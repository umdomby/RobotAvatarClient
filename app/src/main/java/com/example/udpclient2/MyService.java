//https://www.programcreek.com/java-api-examples/?api=com.MAVLink.Parser

package com.example.udpclient2;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.example.udpclient2.event.AxisYZMainActivityMyService;
import com.example.udpclient2.event.AzimutMainActivityMyService;
import com.example.udpclient2.event.JoyUsbServiceMyService;
import com.example.udpclient2.event.SpeechButtonUsbServiceMyActivity;
import com.example.udpclient2.event.SpeechMainActivityMyService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;

public class MyService extends Service {
    public MyService() {
    }
    private final static String TAG = MyService.class.getSimpleName();
    static final int UdpServerPORT = 7878;
    MyService.UdpClientThread udpClientThread;
    final String LOG_TAG = "myLogs";
    //private StringBuilder sb = new StringBuilder(); //отправка данных UDP WiFi
    private TextToSpeech ttx;

    ByteArrayOutputStream byteArrayOutputStreamGY = new ByteArrayOutputStream(256);
    String azimut;
    String AxisYZ;
    String Speech;


    //byte dataUDPSend;
    byte[] dataUDP = new byte[256];
    private boolean running;

    DatagramSocket socket;
    InetAddress address;

    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        readFlags(flags);
        Log.d(LOG_TAG, "onStartCommand");

        ttx =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Locale locale = new Locale("ru");
                    ttx.setLanguage(locale);
                }
            }
        });

        //return START_STICKY;
        return MyService.START_STICKY;
    }


    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void readFlags(int flags){
        if ((flags&START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY)
            Log.d(LOG_TAG, "START_FLAG REDELIVERY");
        if ((flags&START_FLAG_RETRY) == START_FLAG_RETRY)
            Log.d(LOG_TAG, "START_FLAG_RETRY");
    }

//отправка UDP
    public class UdpClientThread extends Thread{
        final String LOG_TAG = "myLogs";
        private boolean running;

        public UdpClientThread(byte data[]) {
            dataUDP = data;
        }
        public void setRunning(boolean running){
            this.running = running;
        }

        @Override
        public void run() {
            running = true;

            try {
                socket = new DatagramSocket();
                // send request
                //byte[] buf = new byte[256];
                //buf = dataUDP;
                //InetAddress IPAddress = InetAddress.getByName("375333752202.dyndns.mts.by");
                InetAddress IPAddress = InetAddress.getByName("192.168.0.123");
                DatagramPacket packet = new DatagramPacket(dataUDP, dataUDP.length,  IPAddress, 7878);
                socket.send(packet);


                // get response
//                byte[] buf2 = new byte[256];
//                DatagramPacket packet2 = new DatagramPacket(buf2, buf2.length);
//                socket.receive(packet2);
//                EventBus.getDefault().post(new ServiceUDPGetEvent(buf2));

                //String str = new String(packet2.getData(), packet2.getOffset(), packet2.getLength());

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(socket != null){
                    socket.close();
                }
            }
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(JoyByteEvent event) {
//        udpClientThread = new MyService.UdpClientThread(event.getData());
//        udpClientThread.start();
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(ServiceUDPSetEvent event) {
//        udpClientThread = new MyService.UdpClientThread(event.getData());
//        udpClientThread.start();
//    }



//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(GYMainActivityMyService event) {
//        GYonEvent = event.getData();
//
//    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SpeechMainActivityMyService event) {
        Speech = event.getData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AxisYZMainActivityMyService event) {
        AxisYZ = event.getData();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AzimutMainActivityMyService event) {
        azimut = event.getData();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(JoyUsbServiceMyService event) {

        String aa = "," + azimut + "," + AxisYZ + "," + Speech + ",";

        String data;
        try {
            byteArrayOutputStreamGY.write(event.getData());
            byteArrayOutputStreamGY.write(aa.getBytes());

            //data = new String(byteArrayOutputStreamGY.toByteArray(), "UTF-8");
            //System.out.print(data);
            udpClientThread = new MyService.UdpClientThread(byteArrayOutputStreamGY.toByteArray());
            udpClientThread.start();


            byteArrayOutputStreamGY.reset();


        } catch (IOException e) {
            e.printStackTrace();
        }

//        udpClientThread = new MyService.UdpClientThread(event.getData());
//        udpClientThread.start();






//        for(int i = 0 ; i < event.getData().length; i++) {
//            System.out.print((char)event.getData()[i] + " ");
//        }

//        System.out.println((char)event.getData()[0] + "  " + (char)event.getData()[1]);
//        System.out.println((char)event.getData()[2] + "  " + (char)event.getData()[3]);
    }


    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
