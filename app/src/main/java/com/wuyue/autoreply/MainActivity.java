package com.wuyue.autoreply;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String AUTO_FIRST_ADD_REPLY = "auto_first_add_reply";

    public static final String AUTO_OLD_FRIEND_REPLY = "auto_old_friend_reply";

    private Button mAutoSettingBtn;

    private EditText mAutoFirstAddReplyEt;

    private EditText mAutoOldFriendReplyEt;

    private Button mAutoReplySaveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

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

            case R.id.auto_reply_save_btn:

                if (!TextUtils.isEmpty(mAutoFirstAddReplyEt.getText())) {
                    SharePreHelper.getInstance(this).setPref(AUTO_FIRST_ADD_REPLY, mAutoFirstAddReplyEt.getText().toString().trim());
                }

                if (!TextUtils.isEmpty(mAutoOldFriendReplyEt.getText())) {
                    SharePreHelper.getInstance(this).setPref(AUTO_OLD_FRIEND_REPLY, mAutoOldFriendReplyEt.getText().toString().trim());
                }

                Toast.makeText(this,"保存成功！",Toast.LENGTH_LONG).show();

                break;
        }
    }

    private void initListener() {
        mAutoSettingBtn.setOnClickListener(this);
        mAutoReplySaveBtn.setOnClickListener(this);
    }

    private void initView(){

        mAutoSettingBtn = (Button) findViewById(R.id.auto_service_setting);

        mAutoFirstAddReplyEt = (EditText) findViewById(R.id.auto_first_reply_et);

        mAutoOldFriendReplyEt = (EditText) findViewById(R.id.auto_oldfriend_reply_et);

        mAutoReplySaveBtn = (Button) findViewById(R.id.auto_reply_save_btn);

        mAutoFirstAddReplyEt.setHint(SharePreHelper.getInstance(this).getPref(MainActivity.AUTO_FIRST_ADD_REPLY,"新添加好友自动回复内容"));

        mAutoOldFriendReplyEt.setHint(SharePreHelper.getInstance(this).getPref(MainActivity.AUTO_OLD_FRIEND_REPLY,"已经添加好友后的聊天自动回复"));
    }
}
