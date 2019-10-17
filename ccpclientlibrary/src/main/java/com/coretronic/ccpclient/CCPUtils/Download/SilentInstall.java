package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Jones Lin on 2019-08-19.
 */
public class SilentInstall {

    public static boolean startInstall(Context context, String apkPath) {
        installAppWithReflection(context, apkPath);
        return true;
    }

    public static void installAppWithReflection(Context context, String path) {
        String TAG = SilentInstall.class.getSimpleName();
        try {
            Log.e(TAG, "[installAppWithReflection][Strat]");
            installWithSystem(context, path);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void installWithSystem(Context context, String path) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String TAG = SilentInstall.class.getSimpleName();
        Uri packageUri = Uri.fromFile(new File(path));
        PackageManager pkgManager = context.getPackageManager();
        pkgManager.getClass().getMethod("installPackage", new Class[]{Uri.class, IPackageInstallObserver.class, Integer.TYPE, String.class}).invoke(pkgManager, new Object[]{packageUri, null, Integer.valueOf(2), ""});
        Log.e(TAG, "[installWithSystem][END]");
    }
}
