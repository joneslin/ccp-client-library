package com.coretronic.ccpclient.CCPUtils.Detector;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpclient.CCPUtils.Download.VersionUpdateHelper;
import com.coretronic.ccpservice.ICCPAidlInterface;
import java.util.List;

/**
 * Created by Jones Lin on 2019-08-15.
 */
public class CCPDetector {
    public static final String TAG = CCPDetector.class.getSimpleName();

    private Context context = null;
    ICCPAidlInterface iccpAidlInterface = null;
    ServiceConnection serviceConnection = null;
    boolean bindCCPService = false;
    String ccpServiceApkName = "";

    public CCPDetector(Context context, ICCPAidlInterface iccpAidlInterface, ServiceConnection serviceConnection, boolean bindCCPService, String ccpServiceApkName) {
        this.context = context;
        this.iccpAidlInterface = iccpAidlInterface;
        this.serviceConnection = serviceConnection;
        this.bindCCPService = bindCCPService;
        this.ccpServiceApkName = ccpServiceApkName;
    }

    public void startCCPService(){
            Log.d(TAG, "*****starting CCP_Service");
            Intent intent = new Intent(Config.ccpserviceStartAction);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(intent);
            if (bindCCPService) {
                // Bind AIDL.
                if (iccpAidlInterface == null) {
                    Intent it = new Intent();
                    //service action.
                    it.setAction("coretronic.intent.action.aidl");
                    //service package name.
                    it.setPackage("com.coretronic.ccpservice");
                    context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE);
                    Config.isBindService = true;
                }
            }
    }

    public boolean isPackageExist(Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }
}
