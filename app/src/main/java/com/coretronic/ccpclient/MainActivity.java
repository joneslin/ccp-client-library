package com.coretronic.ccpclient;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.coretronic.ccpclient.CCPUtils.Example.LoggerExample;
import com.coretronic.ccpclient.CCPUtils.Interface.CCPAidlInterface;
import com.coretronic.ccpclient.CCPUtils.CCPStarter;
import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpservice.ICCPAidlCallback;
import com.coretronic.ccpservice.ICCPAidlInterface;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CCPAidlInterface {
    private CCPStarter ccpStarter = null;
    private String ccpServiceApkName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO 0.啟動CCP Service所有相關動作
        ccpServiceApkName = "ccpservice_optoma_nosign_1.apk";
        ccpStarter = new CCPStarter(this, this, ccpServiceApkName);
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
                Log.d("AIDL Callback", "CCP Service - Connection Status Code: " + value);
            }

            @Override
            public void serviceString(String value) throws RemoteException {
                Log.d("AIDL Callback", "CCP Service - Get String: " + value);
            }

            @Override
            public void ccpServiceReady(String messageCode) throws RemoteException {
                Log.d("AIDL Callback", "CCP Service - Get Ready") ;
                // TODO 4. 重要：傳驗証資訊，需要傳送四個值，分別為DeviceID(由各單位自行定義) 與 guid、secretKey、connectionString(這三個支為註冊api成功時取得)。
                iccpAidlInterface.sendValidationInfo("Optoma-8D2CEEIABU-3", "3cda3db8-8424-45e4-8a59-782bfcc6a74b", "jlXz/15BIIek2jLfcBRyK1Xbeqwy/xYPpvhdKqcwBxQ=", "HostName=CCP-IoTHub-Dev.azure-devices.net;DeviceId=Optoma-8D2CEEIABU-5;SharedAccessKey=jlXz/15BIIek2jLfcBRyK1Xbeqwy/xYPpvhdKqcwBxQ=" );
            }

            @Override
            public void ccpServiceValidationResult(int statusCode) throws RemoteException {
                Log.d("AIDL Callback", "CCP Service - Validation Status Code: " + statusCode);
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
        if (iccpAidlInterface!=null) {
            try {
                iccpAidlInterface.unregisterCallback(iccpAidlCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        iccpAidlInterface = null;
        iccpAidlCallback = null;
    }
}
