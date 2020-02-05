package com.coretronic.ccpclient;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.coretronic.ccpclient.CCPUtils.Download.PackageHelper;
import com.coretronic.ccpclient.CCPUtils.Download.SilentInstall;
import com.coretronic.ccpclient.CCPUtils.Example.LoggerExample;
import com.coretronic.ccpclient.CCPUtils.Interface.CCPAidlInterface;
import com.coretronic.ccpclient.CCPUtils.CCPStarter;
import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpclient.CCPUtils.Model.Software;
import com.coretronic.ccpservice.ICCPAidlCallback;
import com.coretronic.ccpservice.ICCPAidlInterface;
import com.google.gson.Gson;

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
    }

    //TODO 1.ccp agent service 所需參數
    private ICCPAidlInterface iccpAidlInterface = null;
    private ICCPAidlCallback iccpAidlCallback = null;

    //TODO 2.等待您的APP與ccp agent service建立溝通管道
    @Override
    public void alreadyConnected() {
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
                iccpAidlInterface.sendRegisterInfo("Optoma-"+android.os.Build.SERIAL, "5b2e092f-0751-4480-8154-9dece5398ddf");
//                iccpAidlInterface.sendRegisterInfo("OTAtest"+android.os.Build.SERIAL+"--2", "5b2e092f-0751-4480-8154-9dece5398ddf");
            }

            @Override
            public void apkReadyToInstall(String packageName, String folderPath, String fileName) throws RemoteException {
                Log.d("AIDL Callback", "apkReadyToInstall ");
                //

                String filePath = folderPath +"/"+ fileName;
                Log.d("APK installer", "apkReadyToInstall "+filePath);
                iccpAidlInterface.sendOtaStatus(packageName,"applying","","");
                if(SilentInstall.startInstall(filePath))
                {
                    iccpAidlInterface.sendOtaStatus(packageName,"current","","");
                }
                else
                {
                    iccpAidlInterface.sendOtaStatus(packageName,"error","","");
                }
                //
            }

            @Override
            public void firmwareReadyToInstall(String title, String folderPath, String fileName) throws RemoteException {
                Log.d("AIDL Callback", "firmwareReadyToInstall "+title);
                // TODO: 執行韌體安裝
                iccpAidlInterface.sendFirmwareOtaStatus(title,"applying","","");
            }

            @Override
            public void getSoftwareUpdate(String updateMsg) throws RemoteException {
                Log.d("AIDL Callback", "getSoftwareUpdate: "+updateMsg);
                Software software = new Gson().fromJson(updateMsg, Software.class);

                // TODO: 比對package版本
                String packageName = (software.getTitle() != null && !software.getTitle().equals("")) ? software.getTitle() : "";
                String updatePackageVersion = (software.getVersion() != null && !software.getVersion().equals("")) ? software.getVersion() : "";
                String localPackageVersion = PackageHelper.getVersionName(packageName, getBaseContext());

                if(PackageHelper.isPackageExisted(packageName,getBaseContext()) && localPackageVersion.equals(updatePackageVersion)) {
                    // 已經是最新版本
                    Log.e("ota",packageName + localPackageVersion + "已經是最新版本");
                } else {
                    // TODO: 下載並安裝
                    Log.e("ota",software.getName() + " ver." + localPackageVersion+" --> ver." + updatePackageVersion);
                    OtaHelperExample otaHelper = new OtaHelperExample(getBaseContext());
                    otaHelper.startUpdate(iccpAidlInterface, software,false);
                }
            }

            @Override
            public void getFirmwareUpdate(String updateMsg) throws RemoteException {
                Log.d("AIDL Callback", "getFirmwareUpdate: "+updateMsg);
                Software firmware = new Gson().fromJson(updateMsg, Software.class);
                Log.e("ota",firmware.getName());

                // TODO: 比對package版本
                String packageName = (firmware.getTitle() != null && !firmware.getTitle().equals("")) ? firmware.getTitle() : "";
                String updateFirmwareVersion = (firmware.getVersion() != null && !firmware.getVersion().equals("")) ? firmware.getVersion() : "";
                // TODO: 取得firmware版本
                String localFirmwareVersion = "unknown";

                if(PackageHelper.isPackageExisted(packageName,getBaseContext()) && localFirmwareVersion.equals(updateFirmwareVersion)) {
                    // 已經是最新版本
                    Log.e("ota",packageName + localFirmwareVersion + "已經是最新版本");
                } else {
                    // TODO: 下載並安裝
                    Log.e("ota",firmware.getName() + " ver." + localFirmwareVersion+" --> ver." + updateFirmwareVersion);
                    OtaHelperExample otaHelper = new OtaHelperExample(getBaseContext());
                    otaHelper.startUpdate(iccpAidlInterface, firmware,true);
                }
            }
        };

        // TODO 5. 註冊AIDL CallBack.
        try {
            iccpAidlInterface.registerCallback(iccpAidlCallback);
        } catch (RemoteException e) { }


        //TODO 6.送資料到ccp agent service example.
        try {
            // 傳String至ccp agent Service.
            String sendStringToService = iccpAidlInterface.sendString("test123");

            // 傳Integer至ccp agent Service.
            String sendIntToService = iccpAidlInterface.sendInt(1000);


            // TODO 7.重要：將要被CCP平台控管的android package name，透過beControlledPackageName List回傳給CCP agent service.
            List<String> beControlledPackageName = new ArrayList<>();
            beControlledPackageName.add("com.coretronic.ccpservice");
            beControlledPackageName.add("com.coretronic.ccpclient");
            beControlledPackageName.add("com.CiCS.ProjectorController");
            String sendControlPackageNameStatus = iccpAidlInterface.sendControlPackageNameArray(beControlledPackageName);

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
            iccpAidlInterface.unregisterCallback(iccpAidlCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        iccpAidlInterface = null;
        iccpAidlCallback = null;
    }
}
