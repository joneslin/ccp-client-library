package com.coretronic.ccpclient.CCPUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;
import com.coretronic.ccpclient.CCPUtils.Detector.CCPDetector;
import com.coretronic.ccpclient.CCPUtils.Download.PackageHelper;
import com.coretronic.ccpclient.CCPUtils.Interface.CCPAidlInterface;
import com.coretronic.ccpservice.ICCPAidlInterface;

/**
 * Created by Jones Lin on 2019-08-22.
 */
public class CCPStarter {
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

    public void start(){
        start(Config.RECOMMENDED_CCPSERVICE_VERSION);
    }

    public void start(String targetVer){
        String currentCCPserviceVersion = PackageHelper.getVersionName(Config.ccpservicePackageName, context);
        boolean ccpserciceNeedUpdate=false;
        if(!currentCCPserviceVersion.equals(targetVer)) {
            // CCP service版本不符
            ccpserciceNeedUpdate = true;
            Toast.makeText(context.getApplicationContext(),	"CCP Service need update!!", Toast.LENGTH_SHORT).show();
        }

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iccpAidlInterface = ICCPAidlInterface.Stub.asInterface(iBinder);
                Toast.makeText(context.getApplicationContext(),	"This APP already connected to CCP Service !!", Toast.LENGTH_SHORT).show();
                ccpAidlInterface.alreadyConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        // CCP Detector. 偵測ccp若存在則啟動，cpp不存在則下載並啟動，也一併啟動bind service。
        CCPDetector ccpDetector = new CCPDetector(context, iccpAidlInterface, serviceConnection, true, targetVer);
        ccpDetector.startCCPService(ccpserciceNeedUpdate);
    }
}
