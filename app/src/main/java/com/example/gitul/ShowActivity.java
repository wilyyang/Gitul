package com.example.gitul;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ShowActivity extends AppCompatActivity {
    TextView total_step_text;
    TextView step_text;
    TextView target_step_text;
    TextView target_dist_text;
    TextView chase_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        total_step_text = findViewById(R.id.total_step_text);
        step_text = findViewById(R.id.step_text);
        target_step_text = findViewById(R.id.target_step_text);
        target_dist_text = findViewById(R.id.target_dist_text);
        chase_text = findViewById(R.id.chase_text);

        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitContent();
            }
        });

        // 서비스 시작
        if(!TrackService.isServiceRunning(getApplicationContext())){
            Intent serviceIntent = new Intent(ShowActivity.this,TrackService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }


        // 브로드캐스트 리시버
        LocalBroadcastManager.getInstance(this).registerReceiver(stepReceiver, new IntentFilter("StepServiceFilter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(trackReceiver, new IntentFilter("TrackServiceFilter"));
    }

    private BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String total_step_str = ""+intent.getFloatExtra("total_step", 0.0f);
            String track_step_str = ""+intent.getFloatExtra("track_step", 0.0f);
            total_step_text.setText(total_step_str);
            step_text.setText(track_step_str);
        }
    };

    private BroadcastReceiver trackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String target_step_str =""+intent.getIntExtra("target_step", 0);
            String target_dist_str =""+intent.getFloatExtra("target_dist", 0);
            String chase_value_str =""+intent.getIntExtra("chase_value", 0);
            target_step_text.setText(target_step_str);
            target_dist_text.setText(target_dist_str);
            chase_text.setText(chase_value_str);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(stepReceiver, new IntentFilter("StepServiceFilter"));
        registerReceiver(trackReceiver, new IntentFilter("TrackServiceFilter"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(stepReceiver);
        unregisterReceiver(trackReceiver);
    }

    @Override
    public void onBackPressed() {
        exitContent();
    }

    // 종료시 값 반영
    private void exitContent(){
        // 서비스 종료
        Intent serviceIntent = new Intent(ShowActivity.this,TrackService.class);
        stopService(serviceIntent);

        // 종료 과정
        SharedPreferences pref = getSharedPreferences("value", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        float exit_total_step_value  = Float.valueOf(total_step_text.getText().toString());
        exit_total_step_value  += Float.valueOf(pref.getString("total_step", "0"));
        editor.putString("total_step",""+exit_total_step_value);

        float exit_chase_value  = Float.valueOf(chase_text.getText().toString());
        exit_chase_value  += Float.valueOf(pref.getString("total_chase", "0"));
        editor.putString("total_chase",""+exit_chase_value);
        editor.commit();

        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        startActivity(intent);
        finish();
    }
}
