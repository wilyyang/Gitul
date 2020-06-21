package com.example.gitul;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    TextView total_step_text;
    TextView total_chase_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 누적 점수 현황
        total_step_text = (TextView)findViewById(R.id.total_step_text);
        total_chase_text = (TextView)findViewById(R.id.total_chase_text);

        SharedPreferences pref = getSharedPreferences("value", MODE_PRIVATE);
        String total_step = pref.getString("total_step", "0"); //키값, 디폴트값
        total_step_text.setText(total_step);

        String total_chase = pref.getString("total_chase", "0"); //키값, 디폴트값
        total_chase_text.setText(total_chase);

        // 리셋 버튼 이벤트
        Button btn_reset_total_step = (Button) findViewById(R.id.btn_reset_total_step);
        btn_reset_total_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("value", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("total_step","0" );
                editor.commit();
                total_step_text.setText("0");
            }
        });
        Button btn_reset_total_chase = (Button) findViewById(R.id.btn_reset_total_chase);
        btn_reset_total_chase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("value", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("total_chase","0" );
                editor.commit();
                total_chase_text.setText("0");
            }
        });

        // 시작 버튼
        Button btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 액티비티 전환
                Intent intent = new Intent(getApplicationContext(), ShowActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
