package com.example.udpclient2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.udpclient2.compass_pack.Compass;
import com.example.udpclient2.compass_pack.SOTWFormatter;
import com.example.udpclient2.event.AxisYZMainActivityMyService;
import com.example.udpclient2.event.AzimutMainActivityMyService;
import com.example.udpclient2.event.ServiceUDPGetEvent;
import com.example.udpclient2.event.SpeechButtonUsbServiceMyActivity;
import com.example.udpclient2.event.SpeechMainActivityMyService;
import com.example.udpclient2.joy.Dpad;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  implements
        RecognitionListener {
    Dpad dpad = new Dpad();
    byte dataSendUdp[] = new byte[21];
    private WebView webView,webView2;
    String SpeechButton = "start";
    boolean SpeechBool = true;
    boolean SpeechBool2 = false;
    boolean SpeechBool3 = false;

    //speech
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView text1,textSpeech;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    String text;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SpeechButtonUsbServiceMyActivity event) {
        SpeechButton = event.getData();
        text1.setText(SpeechButton);

//        if(SpeechButton.equals("99")){
//            if(SpeechBool == true){
//                speech.startListening(recognizerIntent);
//                SpeechBool = false;
//                SpeechBool2 = true;
//            }
//        }
//        if(SpeechButton.equals("100")){
//            if(SpeechBool2 == true){
//                speech.cancel();
//                SpeechBool2 = false;
//                SpeechBool = true;
//            }
//        }
    }

    private void resetSpeechRecognizer() {
            if (speech != null)
                speech.destroy();
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
            if (SpeechRecognizer.isRecognitionAvailable(this))
                speech.setRecognitionListener(this);
            else
                finish();
    }

    private void setRecogniserIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "ru");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    //USB
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    //Compass
    private Compass compass;
    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;
    private TextView sotwLabel;

    //Sensor
    SensorManager sensorManager;
    Sensor sensorAccel;
    Sensor sensorMagnet;

    //private TextView textjoy1,textjoy2,textjoy3,textjoy4,textjoy5,textjoy6,textjoy7,textjoy8,sensor;
    private final static String TAG = MainActivity.class.getSimpleName();
    TextView infoIp, infoPort;
    TextView textViewState, textViewPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, MyService.class));
        my_temer();
        text1 = (TextView) findViewById(R.id.text1);
//        textjoy1 = findViewById(R.id.textjoy1);textjoy2 = findViewById(R.id.textjoy2);textjoy3 = findViewById(R.id.textjoy3);textjoy4 = findViewById(R.id.textjoy4);
//        textjoy5 = findViewById(R.id.textjoy5);textjoy6 = findViewById(R.id.textjoy6);textjoy7 = findViewById(R.id.textjoy7);textjoy8 = findViewById(R.id.textjoy8);
//        sensor = findViewById(R.id.sensor);
//        infoIp.setText(getIpAddress());
//        infoPort.setText(String.valueOf(UdpServerPORT));
        EventBus.getDefault().register(this);

        //Compass
        sotwFormatter = new SOTWFormatter(this);
        //sotwLabel = findViewById(R.id.sotw_label);
        setupCompass();

        //Sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL);

        //USB
        mHandler = new MyHandler(this);
        display = (TextView) findViewById(R.id.textView1);

//        editText = (EditText) findViewById(R.id.editText1);
//        Button sendButton = (Button) findViewById(R.id.buttonSend);
//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!editText.getText().toString().equals("")) {
//                    String data = editText.getText().toString();
//                    if (usbService != null) { // if UsbService was correctly binded, Send data
//                        usbService.write(data.getBytes());
//                    }
//                }
//            }
//        });

        //speech
        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        amanager.setStreamMute(AudioManager.STREAM_RING, true);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        // UI initialisation
        textSpeech = findViewById(R.id.textSpeech);
        // start speech recogniser
        resetSpeechRecognizer();
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        setRecogniserIntent();
        speech.startListening(recognizerIntent);



        //Camera
        webView = findViewById(R.id.webView);
        // включаем поддержку JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        // указываем страницу загрузки
        webView.loadUrl("http://192.168.0.123:8080/browserfs.html");

        webView2 = findViewById(R.id.webView2);
        // включаем поддержку JavaScript
        webView2.getSettings().setJavaScriptEnabled(true);
        webView2.setWebChromeClient(new WebChromeClient());
        // указываем страницу загрузки
        webView2.loadUrl("http://192.168.0.123:8080/browserfs.html");

    }

    //speech
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speech.startListening(recognizerIntent);
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                        .LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void my_temer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //EventBus.getDefault().post(new ServiceUDPSetEvent(dataSendUdp));
                        getDeviceOrientation();
                    }
                });
            }
        }, 0, 5);


    }

    //Sensor
    void getDeviceOrientation() {
        //Определяет ориентацию дивайся без учёта поворота экрана
        SensorManager.getRotationMatrix(r, null, valuesAccel, valuesMagnet);
        //Метод который который берёт данные ускорения и магнитного поля и формирует из них матрицу данных в r
        SensorManager.getOrientation(r, valuesResult);
        //Из этой матрицы позволяет получить массив значений в радианах и поворота 3-х осей
        valuesResult[0] = (float) Math.toDegrees(valuesResult[0]);
        valuesResult[1] = (float) Math.toDegrees(valuesResult[1]);
        valuesResult[2] = (short) Math.toDegrees(valuesResult[2]);
        //result = (int)valuesResult[0];//1
//        dataSendUdp[11] = (byte)((short)valuesResult[1]);
//        sensor.setText(String.valueOf((int)valuesResult[1])+ " " + (int)valuesResult[2]);
//        dataSendUdp[12] = (byte)((short)valuesResult[2] >> 8);
//        dataSendUdp[13] = (byte)((short)valuesResult[2]);
        //tvText1.setText(Integer.toString(result));

        EventBus.getDefault().post(new AxisYZMainActivityMyService(String.valueOf((int)valuesResult[1] + "," + (int)valuesResult[2])));


        return;
    }

    float[] r = new float[9];
    float[] valuesAccel = new float[3];
    float[] valuesMagnet = new float[3];
    float[] valuesResult = new float[3];

    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i=0; i < 3; i++){
                        valuesAccel[i] = event.values[i];
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for (int i=0; i < 3; i++){
                        valuesMagnet[i] = event.values[i];
                    }
                    break;
            }
        }
    };

//    public void button_onClickStart(View v) {
//        startService(new Intent(this, MyService.class));
//    }
//    public void button_onClickStop(View v) {
//        stopService(new Intent(this, MyService.class));
//    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ServiceUDPGetEvent event) {
        //text1.setText(String.valueOf(event.getData()[1]));
    }

    //joy
    public ArrayList<Integer> getGameControllerIds() {
        ArrayList<Integer> gameControllerDeviceIds = new ArrayList<Integer>();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }
            }
        }
        return gameControllerDeviceIds;
    }

    //event
    public boolean onGenericMotionEvent(MotionEvent event) {
        int press = dpad.getDirectionPressed(event);
        dataSendUdp[1] = (byte)press;
        //EventBus.getDefault().post(new JoyByteEvent(k));
        //textjoy1.setText(String.valueOf(press));

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                processJoystickInput(event, i);
            }
            processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    //buttons
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            dataSendUdp[2] = (byte)keyCode;
            //EventBus.getDefault().post(new JoyByteEvent(p));
            //textjoy2.setText(String.valueOf(keyCode));
//            switch(keyCode){
//                case 109:
//                    // реакция на кнопку с кодом 109
//                    break;
//                case 108:
//                    //реакция на кнопку с кодом 108
//                    break;
//            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //axis_center
    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {
        InputDevice mInputDevice = event.getDevice();
        //stick_1_x
        float x = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X, historyPos);
        dataSendUdp[3] = (byte)(x * 100 * 0.9);
        //textjoy3.setText(String.valueOf(x));

        if (x == 0) {
            x = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x == 0) {
            x = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_Z, historyPos);
        }
        //stick_1_y
        float y = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y, historyPos);
        dataSendUdp[4] = (byte)(y * 100 * 0.9);
        //textjoy4.setText(String.valueOf(y));
        if (y == 0) {
            y = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y == 0) {
            y = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_RZ, historyPos);
        }

        //stick_2_x
        float z = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Z, historyPos);
        dataSendUdp[5] = (byte)(z * 100 * 0.9);
        //textjoy5.setText(String.valueOf(z));

        if (z == 0) {
            z = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (z == 0) {
            z = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_Z, historyPos);
        }
        //stick_2_y
        float rz = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_RZ, historyPos);
        dataSendUdp[6] = (byte)(rz * 100 * 0.9);
        //textjoy6.setText(String.valueOf(rz));
        if (rz == 0) {
            rz = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (rz == 0) {
            rz = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_RZ, historyPos);
        }

        // trigger_l
        float l = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_BRAKE, historyPos);
        dataSendUdp[7]  = (byte)(l * 100 * 2.5);
        //textjoy7.setText(String.valueOf(l));

        if (l == 0) {
            l = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_LTRIGGER, historyPos);
        }
        //trigger_x
        float r = getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_GAS, historyPos);
        dataSendUdp[8] = (byte)(r * 100 * 2.5);
        //textjoy8.setText(String.valueOf(r));

        if (r == 0) {
            r = getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_RTRIGGER, historyPos);
        }
    }
    //joyEnd

    //Compas
    private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }
    private void adjustArrow(float azimuth) {
//        Log.d(TAG, "will set rotation from " + currentAzimuth + " to "
//                + azimuth);

        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        //arrowView.startAnimation(an);
    }

    private void adjustSotwLabel(short azimuth) {
        //sotwLabel.setText(String.valueOf(azimuth));
        //sotwLabel.setText(String.valueOf(dataSendUdp[9]));

//        dataSendUdp[9] = (byte)((azimuth >> 8) & 0xff);
//        dataSendUdp[10] = (byte)(azimuth & 0xff);

        EventBus.getDefault().post(new AzimutMainActivityMyService(String.valueOf(azimuth)));
    }

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow((short)azimuth);
                        adjustSotwLabel((short)azimuth);

                    }
                });
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        compass.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
        speech.stopListening();
    }
    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        compass.stop();
        if (speech != null) {
            speech.destroy();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //USB
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onError(int errorCode) {
        //audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0,  AudioManager.FLAG_SHOW_UI);
        //String errorMessage = getErrorText(errorCode);
        //Log.i(LOG_TAG, "FAILED " + errorMessage);
        //returnedError.setText(errorMessage);
        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        //Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        //progressBar.setProgress((int) rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        //progressBar.setIndeterminate(true);
        speech.stopListening();
    }


    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        text = "";

        for (String result : matches) {
            //text += result + "\n";
            if(SpeechBool3 == false) {
                text = result;
                SpeechBool3 = true;
            }
//            if(result.equals("Вася привет")){
//                Toast toast = Toast.makeText(getApplicationContext(),
//                        "Вася Привет", Toast.LENGTH_SHORT);
//                toast.show();
//                //ttx.speak("Сергей привет", TextToSpeech.QUEUE_FLUSH, null);
//            }
            EventBus.getDefault().post(new SpeechMainActivityMyService(text));
            textSpeech.setText(text);
            speech.startListening(recognizerIntent);

        }
        SpeechBool3 = false;
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

}
