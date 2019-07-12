package com.spcfar.lock;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 110;

    private DevicePolicyManager policyManager;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.translucent);
        transParentStatusBarAndBottomNavigationBar();
        Log.d("llll","==onCreate==");

        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);
        boolean active = policyManager.isAdminActive(componentName);
        if (!active) {
            openActiveManage();
        } else {
            try {
                WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = windowManager.getDefaultDisplay();
                @SuppressLint({"NewApi", "LocalSuppress"})
                int state = display.getState();
                if (state == 2) {
                    policyManager.lockNow();
                }
            } catch (Exception e) {
            } catch (NoSuchMethodError e) {
            }
        }
        killSelf();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (!policyManager.isAdminActive(componentName)) {   //若无权限
                killSelf();
            } else {
                policyManager.lockNow();//直接锁屏
            }
        } else {
            killSelf();
        }
    }

    private void openActiveManage() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活以使用锁屏功能");
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void transParentStatusBarAndBottomNavigationBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        hideActionBar();
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void killSelf() {
        policyManager = null;
        componentName = null;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        policyManager = null;
        componentName = null;
        Log.d("llll","==onDestroy==");
    }
}
