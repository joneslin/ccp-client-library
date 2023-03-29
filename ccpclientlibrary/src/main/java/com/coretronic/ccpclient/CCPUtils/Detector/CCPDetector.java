package com.coretronic.ccpclient.CCPUtils.Detector;

jonesimport android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.coretronic.ccpclient.CCPUtils.Config;
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

    public void startCCPService(boolean ccpserciceNeedUpdate){
        boolean isPackageExist = isPackageExist(context, Config.ccpservicePackageName);

        ///CCP app是否存在.
        if (isPackageExist && !ccpserciceNeedUpdate){
            Log.d(TAG, "Bind CCP_Service");
            Intent serviceIntentCcpservice = new Intent();
            String pkg = "com.coretronic.ccpservice";
            String cls = "com.coretronic.ccpservice.BroadcastReceiver.ShadowIntentReceiver";
            serviceIntentCcpservice.setComponent(new ComponentName(pkg, cls));
            context.sendBroadcast(serviceIntentCcpservice);

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
