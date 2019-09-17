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

//public class MainActivity extends AppCompatActivity{
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        //### 0.啟動CCP Service所有相關動作
//        CCPStarter ccpStarter = new CCPStarter(this);
//        ccpStarter.start();
//    }
//}

public class MainActivity extends AppCompatActivity implements CCPAidlInterface {
    private CCPStarter ccpStarter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

//TODO 0.啟動CCP Service所有相關動作
        ccpStarter = new CCPStarter(this, this);
        ccpStarter.start();

        // write log example.
        LoggerExample loggerExample = new LoggerExample();
        loggerExample.saveLogToFile(this);
    }

//--------------------------------------------------------------------------------------------------
//若需要APP與CCP Service雙向溝通，才需加入以下程式與implements CCPAidlInterface。
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
                Log.d("AIDL Resutl", "DataFromService: " + value);
            }

            @Override
            public void serviceString(String value) throws RemoteException {
                Log.d("AIDL Resutl", "DataFromService: " + value);
            }
        };

        try {
            iccpAidlInterface.registerCallback(iccpAidlCallback);
        } catch (RemoteException e) { }


//TODO 4.送資料到ccp agent service
        try {
            // 傳一般string至ccp agent Service.
            String sendStringToService = iccpAidlInterface.sendString("test123");

            // 傳一般integer至ccp agent Service.
            String sendIntToService = iccpAidlInterface.sendInt(1000);

// TODO 5.重要：將要被控管的android package name，透過beControlledPackageName List回傳給ccp agent service.
            List<String> beControlledPackageName = new ArrayList<>();
            beControlledPackageName.add("com.coretronic.ccpservice");
            beControlledPackageName.add("com.coretronic.fusion");
            String sendControlPackageNameStatus = iccpAidlInterface.sendControlPackageNameArray(beControlledPackageName);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Client", "onDestroy: " + Config.isBindService);
//TODO 6.解除CCP Service綁定
        if (Config.isBindService) {
            unbindService(ccpStarter.getServiceConnection());
        }

//TODO 7.解除CCP AIDL Callback.
        try {
            iccpAidlInterface.unregisterCallback(iccpAidlCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        iccpAidlInterface = null;
        iccpAidlCallback = null;
    }
}
