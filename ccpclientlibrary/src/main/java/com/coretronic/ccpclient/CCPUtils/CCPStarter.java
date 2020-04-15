package com.coretronic.ccpclient.CCPUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.Toast;
import com.coretronic.ccpclient.CCPUtils.Detector.CCPDetector;
import com.coretronic.ccpclient.CCPUtils.Download.PackageHelper;
import com.coretronic.ccpclient.CCPUtils.Interface.CCPAidlInterface;
import com.coretronic.ccpservice.ICCPAidlInterface;

/**
 * Created by Jones Lin on 2019-08-22.
 */
public class CCPStarter {
    private String TAG = CCPStarter.class.getSimpleName();
    Context context;
    ICCPAidlInterface iccpAidlInterface = null;
    ServiceConnection serviceConnection = null;
    CCPAidlInterface ccpAidlInterface;
    public ICCPAidlInterface getIccpAidlInterface() {
        return iccpAidlInterface;
    }
    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public CCPStarter(Context context, CCPAidlInterface ccpAidlInterface) {
        this.context = context;
        this.ccpAidlInterface = ccpAidlInterface;
    }

    public CCPStarter(Context context) {
        this.context = context;
    }

    public boolean start(){
        return start(Config.RECOMMENDED_CCPSERVICE_VERSION, Config.Environment.Production, false);
    }

    public boolean start(String targetVer, Config.Environment environment, boolean useAndroidApi19){
        String envString;
        switch(environment) {
            case Production:
                envString = "";
                break;
            case Development:
                envString = "-dev";
                break;
            case POC:
                envString = "-poc";
                break;
            default:
                envString = "";
                break;
        }

        String api19String = (useAndroidApi19) ? "_api19" : "";
        String targetVersionName = targetVer + api19String + envString;
        String currentCCPserviceVersion = PackageHelper.getVersionName(Config.ccpservicePackageName, context);
        boolean ccpserciceNeedUpdate=false;
        if(!currentCCPserviceVersion.equals(targetVersionName)) {
            // CCP service版本不符
            ccpserciceNeedUpdate = true;
        }

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iccpAidlInterface = ICCPAidlInterface.Stub.asInterface(iBinder);
//                try {
//                    iBinder.linkToDeath(mDeathRecipient,0);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
                ccpAidlInterface.alreadyConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        // CCP Detector. 偵測ccp若存在則啟動，cpp不存在則下載並啟動，也一併啟動bind service。
        CCPDetector ccpDetector = new CCPDetector(context, iccpAidlInterface, serviceConnection, true, targetVersionName);
        ccpDetector.startCCPService(ccpserciceNeedUpdate);
        return ccpserciceNeedUpdate;
    }

//    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
//
//        @Override
//        public void binderDied() {                  // 当绑定的service异常断开连接后，会自动执行此方法
//            Log.e(TAG,"enter Service binderDied " );
//            if (iccpAidlInterface != null){
//                iccpAidlInterface.asBinder().unlinkToDeath(mDeathRecipient, 0);
//                //  重新绑定服务端的service
//                Intent it = new Intent();
//                //service action.
//                it.setAction("coretronic.intent.action.aidl");
//                //service package name.
//                it.setPackage("com.coretronic.ccpservice");
//                context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE);
//            }
//        }
//    };
}
