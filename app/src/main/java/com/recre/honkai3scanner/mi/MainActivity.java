package com.recre.honkai3scanner.mi;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.king.camera.scan.CameraScan;
import com.recre.honkai3scanner.mi.utils.MihoyoSDK;
import com.recre.honkai3scanner.mi.utils.Tools;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //小米uid
    private String uid;
    //小米session
    private String session;
    //deviceId设备id
    private String deviceId;

    private boolean miLoginStatus = false;

    private Button loginButton;


    //申请权限
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    //同意权限
                    scanStart();
                } else {
                    //不同意权限
                    Toast.makeText(this, "没有权限不能使用/_ \\", Toast.LENGTH_SHORT).show();
                }
            });

    //相机页面回调
    private final ActivityResultLauncher<Intent> scanStartLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            //获得结果
            String scanResult = result.getData().getStringExtra(CameraScan.SCAN_RESULT);
            //获得二维码url参数
            Map<String, Object> qRCodeMap = Tools.parseQRCodeUrl(scanResult);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    while (MihoyoSDK.status!=3){
                        if (MihoyoSDK.status==0){
                            //登录游戏
                            MihoyoSDK.verify(deviceId, uid, session);
                        }else if (MihoyoSDK.status==1){
                            //启动登录
                            MihoyoSDK.scanLogin(qRCodeMap.get("ticket"), deviceId);
                        } else if (MihoyoSDK.status==2) {
                            MihoyoSDK.scanLoginConfirm(qRCodeMap.get("ticket"), deviceId);
                        }
                        count++;
                        if (count==5){
                            runOnUiThread(() -> {
                                Toast.makeText(getBaseContext(),"登录失败",Toast.LENGTH_SHORT).show();
                                loginButton.setEnabled(true);
                            });
                            break;
                        }
                    }
                }
            }).start();

        } else {
            //无结果
            Toast.makeText(getBaseContext(), "扫描失败", Toast.LENGTH_SHORT).show();
            loginButton.setEnabled(true);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //获取deviceId
        deviceId = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //小米游戏SDK隐私合规
        MiCommplatform.getInstance().onUserAgreed(this);
        //登录小米账号
        miLogin();


        //按键响应
        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(v -> {
            loginButton.setEnabled(false);
            //未登录
            if (!miLoginStatus){
                miLogin();
                return;
            }
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //无权限，进行申请
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                //有权限
                scanStart();
            }

        });
    }

    //启动扫码
    private void scanStart() {
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        scanStartLauncher.launch(intent);
    }

    //小米账号登录
    private void miLogin() {
        MiCommplatform.getInstance().miLogin(this,
                (code, arg1) -> {
                    switch (code) {
                        case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS: // 登陆成功
                            //获取用户的登陆后的UID（即用户唯一标识）
                            uid = arg1.getUid();

                            //以下为获取session并校验流程，如果是网络游戏必须校验，如果是单机游戏或应用可选//
                            //获取用户的登陆的Session（请参考3.3 用户session验证接口）
                            session = arg1.getSessionId();

                            miLoginStatus = true;
                            //请开发者完成将uid和session提交给开发者自己服务器进行session验证
                            loginButton.setEnabled(true);
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                            // 取消登录
                            Toast.makeText(getBaseContext(), "取消登录", Toast.LENGTH_SHORT).show();
                            loginButton.setEnabled(true);
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                            //登录操作正在进行中
                            Toast.makeText(getBaseContext(), "登录操作正在进行中", Toast.LENGTH_SHORT).show();
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                            // 登陆失败
                        default:
                            // 登录失败
                            Toast.makeText(getBaseContext(), "登陆失败,请检查网络ᓚᘏᗢ", Toast.LENGTH_SHORT).show();
                            loginButton.setEnabled(true);
                            break;
                    }
                });

    }


    //返回键响应
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            MiCommplatform.getInstance().miAppExit(this, code -> {
                if (code == MiErrorCode.MI_XIAOMI_EXIT) {
                    finish();
                }
            });
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}