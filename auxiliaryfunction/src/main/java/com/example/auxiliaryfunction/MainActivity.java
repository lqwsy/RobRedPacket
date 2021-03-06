package com.example.auxiliaryfunction;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检测服务是否开启
        if(accessibilityServiceIsRunning()){
            Toast.makeText(this, "服务已开启", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"服务未开启",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 跳转开启系统辅助服务
     * */
    public void openAccessibilityOperator(View view){
        new AlertDialog.Builder(this)
                .setTitle("开启辅助功能")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("使用此项功能需要您开启辅助功能")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 隐式调用系统设置界面
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    }
                }).create().show();
    }

    /**
     * 判断辅助服务是否开启
     * */
    private boolean accessibilityServiceIsRunning(){
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(Short.MAX_VALUE);
        for(ActivityManager.RunningServiceInfo info : services){
            if(info.service.getClassName().equals(getPackageName()+".MyAccessibilityService")){
                return true;
            }
        }
        return false;
    }

    //获取主页面
    public Activity getInstance(){
        return this;
    }

}
