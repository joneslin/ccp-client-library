package com.coretronic.ccpclient;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
            public void serviceString(String value) throws RemoteException {
                Log.d("AIDL Callback", "Get String From CCP Service: " + value);
            }

            @Override
            public void ccpServiceReady(String messageCode) throws RemoteException {
                Log.d("AIDL Callback", "CCP Service Get Ready, please send DeviceID and TenantID") ;

                // TODO 4. 重要：傳註冊資訊，需要傳送兩個值，分別為DeviceID(由各單位自行定義) 與 TenantID(範例中為optoma TenantID)。
//                iccpAidlInterface.sendRegisterInfo("Optoma-"+android.os.Build.SERIAL, "5b2e092f-0751-4480-8154-9dece5398ddf");
//                iccpAidlInterface.sendRegisterInfo("OTAtest"+android.os.Build.SERIAL+"-1", "5b2e092f-0751-4480-8154-9dece5398ddf");
                iccpAidlInterface.sendRegisterInfo("OTAtest"+android.os.Build.SERIAL+"-2", "00000000-0000-0000-0000-000000000002");
            }

            @Override
            public void getOtaInfo(String latestOTAByDeviceStr) throws RemoteException {
                if(latestOTAByDeviceStr==null){
                    Log.e("OTA info","request OTA info failed");
                    return;
                }
                Log.d("AIDL Callback", "Get Ota info From CCP Service: " + latestOTAByDeviceStr);
                LatestOTAByDeviceGson latestOTAByDeviceGson = new Gson().fromJson(latestOTAByDeviceStr, LatestOTAByDeviceGson.class);
                Log.d("OTA info", "Product name: " + latestOTAByDeviceGson.getProduct().getName());
                // Firmware Info
                LatestOTAByDeviceGson.FirmwareBean firmware = latestOTAByDeviceGson.getFirmware();
                Log.d("OTA info", "Firmware title: " + firmware.getTitle());
                Log.d("OTA info", "Firmware name: " + firmware.getName());
                Log.d("OTA info", "Firmware version: " + firmware.getVersion());
                Log.d("OTA info", "Firmware uri: " + firmware.getUri());
                // TODO: 版本比對
                String localFirmwareVersion = "";
                if(!localFirmwareVersion.equals(firmware.getVersion())) {
                    // TODO: 下載並安裝firmware
                    OtaHelperExample otaHelper = new OtaHelperExample(getBaseContext());
                    otaHelper.startUpdateFiirmware(iccpAidlInterface, firmware, localFirmwareVersion);
                } else {
                    Log.d("OTA info",firmware.getTitle() + localFirmwareVersion + "已經是最新版本");
                }

                for (LatestOTAByDeviceGson.SoftwaresBean software : latestOTAByDeviceGson.getSoftwares()) {
                    Log.d("OTA info", "Software title: " + software.getTitle());
                    Log.d("OTA info", "Software name: " + software.getName());
                    Log.d("OTA info", "Software version: " + software.getVersion());
                    Log.d("OTA info", "Software uri: " + software.getUri());

                    // TODO: 比對package版本
                    String packageName = (software.getTitle() != null && !software.getTitle().equals("")) ? software.getTitle() : "";
                    String updatePackageVersion = (software.getVersion() != null && !software.getVersion().equals("")) ? software.getVersion() : "";
                    String localPackageVersion = PackageHelper.getVersionName(packageName, getBaseContext());

                    if(PackageHelper.isPackageExisted(packageName,getBaseContext()) && localPackageVersion.equals(updatePackageVersion)) {
                        // 已經是最新版本
                        Log.d("OTA info",packageName + localPackageVersion + "已經是最新版本");
                    } else {
                        // TODO: 下載並安裝
                        Log.d("OTA info",software.getName() + " ver." + localPackageVersion+" --> ver." + updatePackageVersion);
                        OtaHelperExample otaHelper = new OtaHelperExample(getBaseContext());
                        otaHelper.startUpdateSoftware(iccpAidlInterface, software, localPackageVersion);
                    }
                }
            }

            @Override
            public void ccpServiceValidated() throws RemoteException {
                Log.d("AIDL Callback", "CCP Service Validated") ;
                // TODO: 回傳firmware版本來更新系統資訊
                String firmwareVersoon = "unknown";
                iccpAidlInterface.reportSystemInfo(firmwareVersoon);
            }
        };

        // TODO 5. 註冊AIDL CallBack.
        try {
            iccpAidlInterface.registerCallback(iccpAidlCallback);
        } catch (RemoteException e) { }

        // TODO 設定環境
        try {
            iccpAidlInterface.setEnvironment("development");
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        //TODO 6.送資料到ccp agent service example.
        try {
            // 傳String至ccp agent Service.
            String sendStringToService = iccpAidlInterface.sendString("test123");

            // 傳Integer至ccp agent Service.
            String sendIntToService = iccpAidlInterface.sendInt(1000);


//            // TODO 7.重要：將要被CCP平台控管的android package name，透過beControlledPackageName List回傳給CCP agent service.
//            List<String> beControlledPackageName = new ArrayList<>();
//            beControlledPackageName.add("com.coretronic.ccpservice");
//            beControlledPackageName.add("com.coretronic.ccpclient");
//            beControlledPackageName.add("com.CiCS.ProjectorController");
//            String sendControlPackageNameStatus = iccpAidlInterface.sendControlPackageNameArray(beControlledPackageName);

            if(iccpAidlInterface != null) {
                iccpAidlInterface.requestOtaInfo();
            }

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
                Toast.makeText(context, packageName + "下載&安裝成功", Toast.LENGTH_SHORT).show();
                try {
                    iccpAidlInterface.sendOtaStatus(packageName,"current", PackageHelper.getVersionName(packageName, context), "");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
