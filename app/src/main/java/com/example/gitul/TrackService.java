package com.example.gitul;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TrackService extends Service implements SensorEventListener {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // <공통 파트>
    public static boolean isServiceRunning(Context context)
    {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE))
        {
            if (TrackService.class.getName().equals(rsi.service.getClassName()))
                return true;
        }
        return false;
    }

    private static final int ONGOING_NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, ShowActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                    new NotificationCompat.Builder(this, "Gitul")
                            .setContentTitle("Gitul")
                            .setContentText("Track Service")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pendingIntent)
                            .setTicker("Ongoing")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 센서
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepDetectSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, stepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // 스레드
        TrackServiceHandler handler = new TrackServiceHandler();
        targetThread = new TargetThread(handler);
        targetThread.start();
        return START_STICKY;
    }

    public void onDestroy() {
        unRegistManager();

        targetThread.stopForever();
        targetThread = null; //빠른 회수
        Log.d("Track", "Service Destroy");
    }
    // </공통 파트>

    // <센서 파트>
    private SensorManager sensorManager;
    private Sensor stepDetectSensor;
    private float total_step_value;
    private float track_step_value;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            if (sensorEvent.values[0] == 1.0f) {
                total_step_value  += sensorEvent.values[0];
                track_step_value += sensorEvent.values[0];

                Intent intent = new Intent("StepServiceFilter");
                intent.putExtra("total_step", total_step_value);
                intent.putExtra("track_step", track_step_value);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
    public void unRegistManager() {
        try {
            sensorManager.unregisterListener(this);
            Log.d("Track", "Sensor Unregister");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // </센서 파트>

    // <타겟 파트>
    TargetThread targetThread;
    class TargetThread extends Thread{
        Handler handler;
        boolean isRun = true;
        public TargetThread(Handler handler){
            this.handler = handler;
        }
        public void stopForever(){
            synchronized (this) {
                this.isRun = false;
            }
        }
        public void run(){
            while(isRun){
                handler.sendEmptyMessage(0);
                try{
                    Thread.sleep(1000);
                }catch (Exception e) {}
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            Log.d("Track", "Thread Interrupt");
        }
    }
    // </타겟 파트>



    // <처리 파트>
    class TrackServiceHandler extends Handler {
        private int target_speed = 1;
        private final int TARGET_DIST_INIT = 5;
        private int target_step = TARGET_DIST_INIT;
        private int chase_value = 0;

        @Override
        public void handleMessage(android.os.Message msg) {
            target_step += target_speed;
            float temp_track_step_value = track_step_value;
            float temp_target_step = target_step;

            float target_dist = temp_track_step_value - temp_target_step;

            if(target_dist>0) {
                chase_value++;
                target_step = TARGET_DIST_INIT;
                track_step_value = 0;
            }else if(target_dist<-100){
                chase_value--;
                target_step = TARGET_DIST_INIT;
                track_step_value = 0;
            }

            // 쇼액티비티 값 갱신
            Intent broadIntent = new Intent("TrackServiceFilter");
            broadIntent.putExtra("target_step", target_step);
            broadIntent.putExtra("target_dist", target_dist);
            broadIntent.putExtra("chase_value", chase_value);
            LocalBroadcastManager.getInstance(TrackService.this).sendBroadcast(broadIntent);
        }
    };
    // </처리 파트>
}
