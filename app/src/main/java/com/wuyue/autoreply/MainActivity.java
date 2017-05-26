package com.wuyue.autoreply;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mAutoSettingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAutoSettingBtn = (Button) findViewById(R.id.auto_service_setting);
        initListener();

    }

    private void checkServiceWorking() {
        Context ct = getApplicationContext();
        boolean serviceIsWork = ActionUtils.isServiceWork(ct, AutoReplyService.class.getName());
        if (serviceIsWork == true) {
            mAutoSettingBtn.setBackgroundColor(Color.parseColor("#00aa00"));
            mAutoSettingBtn.setText("自动化服务已开启，点击去设置");
        } else {
            mAutoSettingBtn.setBackgroundColor(Color.parseColor("#aa0000"));
            mAutoSettingBtn.setText("自动化服务已停止，点击去设置");
        }
        mAutoSettingBtn.getBackground().setAlpha(128);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkServiceWorking();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auto_service_setting:
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                break;
        }
    }

    private void initListener() {
        mAutoSettingBtn.setOnClickListener(this);
    }
}
