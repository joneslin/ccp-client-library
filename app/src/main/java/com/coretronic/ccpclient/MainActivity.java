package com.coretronic.ccpclient;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.coretronic.ccpclient.CCPUtils.Download.PackageHelper;
import com.coretronic.ccpclient.CCPUtils.Download.SilentInstall;
import com.coretronic.ccpclient.CCPUtils.Example.LoggerExample;
import com.coretronic.ccpclient.CCPUtils.Interface.CCPAidlInterface;
import com.coretronic.ccpclient.CCPUtils.CCPStarter;
import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpclient.CCPUtils.Model.LatestOTAByDeviceGson;
import com.coretronic.ccpclient.CCPUtils.Model.Software;
import com.coretronic.ccpservice.ICCPAidlCallback;
import com.coretronic.ccpservice.ICCPAidlInterface;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CCPAidlInterface {
    private CCPStarter ccpStarter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//TODO 0.啟動CCP Service所有相關動作
        ccpStarter = new CCPStarter(this, this);
        ccpStarter.start();

        // write log example.
        LoggerExample loggerExample = new LoggerExample();
        loggerExample.saveLogToFile(this);

        // register apk install broadcast.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme("package");
        this.registerReceiver(broadcastReceiver, filter);
    }

    //TODO 1.ccp agent service 所需參數
    private ICCPAidlInterface iccpAidlInterface = null;
    private ICCPAidlCallback iccpAidlCallback = null;

    //TODO 2.等待您的APP與ccp agent service建立溝通管道
    @Override
    public void alreadyConnected()  {
        this.iccpAidlInterface = ccpStarter.getIccpAidlInterface();

        //TODO 3.從ccp agent service接收資料
        iccpAidlCallback = new ICCPAidlCallback.Stub() {
            @Override
            public void serviceInt(int value) throws RemoteException {
                Log.d("AIDL Callback", "CCP Service Status Code: " + value);
            }

            @Override
            public void serviceString(String value, String msgId) throws RemoteException {
                Log.d("AIDL Callback", "Get String From CCP Service: " + value);
            }

            @Override
            public void ccpServiceReady(String messageCode) throws RemoteException {
                Log.d("AIDL Callback", "CCP Service Get Ready, please send DeviceID and TenantID") ;

                iccpAidlInterface.sendValidationInfo("Optoma-5E:83:6A:A7:4B:C4", "7b6835b6-b038-4c38-9d25-e3d417facfb8", "1h8kTsCgl0l/e+0eigNj7L0EubE7vANkkH+7JqChKo0=", "HostName=CCP-IoTHub-Dev.azure-devices.net;DeviceId=7b6835b6-b038-4c38-9d25-e3d417facfb8;SharedAccessKey=1h8kTsCgl0l/e+0eigNj7L0EubE7vANkkH+7JqChKo0=" );
            }

            @Override
            public void getOtaInfo(String latestOTAByDeviceStr) throws RemoteException {

            }

            @Override
            public void ccpServiceValidated(String guid, String secretKey) throws RemoteException {
                Log.d("AIDL Callback", "CCP Service Validated") ;
                // TODO: 回傳firmware版本來更新系統資訊
                String firmwareVersoon = "unknown";
                iccpAidlInterface.reportSystemInfo(firmwareVersoon);
            }

            @Override
            public void ccpServiceValidationResult(int statusCode) throws RemoteException {
                Log.d("AIDL Callback", "CCP ccpServiceValidationResult: " + statusCode) ;
            }
        };

        // TODO 5. 註冊AIDL CallBack.
        try {
            iccpAidlInterface.registerCallback(iccpAidlCallback);
        } catch (RemoteException e) { }


        //TODO 6.送資料到ccp agent service example.
        try {
            // 傳String至ccp agent Service.
            String sendStringToService = iccpAidlInterface.sendString("test123","");

            // 傳Integer至ccp agent Service.
            String sendIntToService = iccpAidlInterface.sendInt(1000);


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Client", "onDestroy: " + Config.isBindService);

        //TODO 8.解除CCP Service綁定
        if (Config.isBindService) {
            unbindService(ccpStarter.getServiceConnection());
        }

        //TODO 9.解除CCP AIDL Callback.
        try {
            if(iccpAidlCallback!=null) {
                iccpAidlInterface.unregisterCallback(iccpAidlCallback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        iccpAidlInterface = null;
        iccpAidlCallback = null;

        //unregister apk install broadcast.
        this.unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                String packageName = intent.getDataString().replace("package:","");
                Log.e("MainActivity", "安装了:" + packageName);
                try {
                    Toast.makeText(context, packageName + "下載&安裝成功", Toast.LENGTH_SHORT).show();
                } catch (WindowManager.BadTokenException e){
                    e.printStackTrace();
                }
            }
        }
    };
}
