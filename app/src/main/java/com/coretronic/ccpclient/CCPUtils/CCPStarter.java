package com.coretronic.ccpclient.CCPUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
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

    public void setIccpAidlInterface(ICCPAidlInterface iccpAidlInterface) {
        this.iccpAidlInterface = iccpAidlInterface;
    }

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

    public boolean start(){

        String currentCCPserviceVersion = PackageHelper.getVersionName(Config.ccpservicePackageName, context);
        boolean ccpserciceNeedUpdate=false;

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iccpAidlInterface = ICCPAidlInterface.Stub.asInterface(iBinder);
//                Toast.makeText(context.getApplicationContext(),	"This APP already connected to CCP Service !!", Toast.LENGTH_SHORT).show();
                ccpAidlInterface.alreadyConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        // CCP Detector. 偵測ccp若存在則啟動，cpp不存在則下載並啟動，也一併啟動bind service。
        CCPDetector ccpDetector = new CCPDetector(context, iccpAidlInterface, serviceConnection);
        ccpDetector.startCCPService(ccpserciceNeedUpdate);
        return ccpserciceNeedUpdate;
    }

    public void stop(){
        if(Config.isBindService){
            try {
                context.unbindService(serviceConnection);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    }
}
