package com.coretronic.ccpclientlibrary.CCPUtils.Detector;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.coretronic.ccpclientlibrary.CCPUtils.Config;
import com.coretronic.ccpclientlibrary.CCPUtils.Download.VersionUpdateHelper;
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

    public CCPDetector(Context context, ICCPAidlInterface iccpAidlInterface, ServiceConnection serviceConnection) {
        this.context = context;
        this.iccpAidlInterface = iccpAidlInterface;
        this.serviceConnection = serviceConnection;
    }

    public void startCCPService(){
        Log.d(TAG, "*****startIoTHubService");

        boolean isPackageExist = isPackageExist(context, Config.ccpservicePackageName);
        Log.d(TAG, "*****isShadowPackageExist: " + isPackageExist);

        ///CCP app是否存在.
        if (isPackageExist){
            Log.d(TAG, "*****starting CCP Service");
            Intent intent = new Intent(Config.ccpserviceStartAction);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(intent);

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
        } else {
            //download apk and start.
            Log.d(TAG, "*****need to Download CCP APK");
            VersionUpdateHelper versionUpdateHelper = new VersionUpdateHelper(context, iccpAidlInterface, serviceConnection);
            versionUpdateHelper.downloadManager("ccpservice.apk", Config.ccpserviceApkDownloadPath, "", true);
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
