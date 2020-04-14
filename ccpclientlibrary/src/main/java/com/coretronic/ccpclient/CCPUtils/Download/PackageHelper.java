package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.pm.PackageInfoCompat;
import android.util.Log;
import java.util.List;

/**
 * Created by Jones Lin on 2019-10-17.
 */
public class PackageHelper {
    public static long getVersionCode(String packageName, Context context) {
        long versionLong = 0L;
        if (isPackageExisted(packageName, context)) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
                versionLong = PackageInfoCompat.getLongVersionCode(pInfo);
                ;
            } catch (PackageManager.NameNotFoundException e1) {
                Log.e("PackageHelper", "package name not found: ", e1);
                return 0;
            }
        }else {
            Log.e("PackageHelper", "package name not found:" + packageName + ", return version code: 0");
            return 0;
        }
        return versionLong;
    }

    public static boolean isPackageExisted(String targetPackage, Context context){
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

    public static String getVersionName(String packageName, Context context) {
        try{
            String versionSting;
            if (isPackageExisted(packageName, context)) {
                try {
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
                    versionSting = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e1) {
                    Log.e("PackageHelper", "package name not found: ", e1);
                    return "0.0";
                }
            }else {
                Log.e("PackageHelper", "package name not found:" + packageName + ", return version name: 0.0");
                return "0.0";
            }
            return versionSting;
        } catch (NullPointerException e){
            e.printStackTrace();
            return "0.0";
        }

    }
}
