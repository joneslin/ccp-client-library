package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

/**
 * Created by Jones Lin on 2019-08-19.
 */
public class SilentInstall {

    public static boolean startInstall(String apkPath) {
        String TAG = SilentInstall.class.getSimpleName();
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        BufferedReader successStream = null;
        Process process = null;
        try {
            // 申请 su 權限
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());

            // 標準安裝：pm install 命令
            String command = "pm install -r " + apkPath + "\n";

            // 安裝至system/priv-app，成為系統app
//            dataOutputStream.writeBytes("mount -o rw,remount -t auto /system"+ "\n");
//            dataOutputStream.flush();
//            dataOutputStream.writeBytes("chmod 777 /system/priv-app" + "\n");
//            dataOutputStream.flush();
//            String command = "mv " + apkPath + " /system/priv-app" + "\n";

            dataOutputStream.write(command.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = errorStream.readLine()) != null) {
                errorMsg.append(line);
            }
            Log.d(TAG, "silent install error message: " + errorMsg);
            StringBuilder successMsg = new StringBuilder();
            successStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 讀取命令執行結果
            while ((line = successStream.readLine()) != null) {
                successMsg.append(line);
            }
            Log.d(TAG, "silent install success message: " + successMsg);
            // 如果執行結果中包含 Failure 字樣就認為是操作失敗，否則就認為安裝成功
            if (!(errorMsg.toString().contains("Failure") || successMsg.toString().contains("Failure") || errorMsg.toString().contains("Killed"))) {
                result = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
                if (successStream != null) {
                    successStream.close();
                }
            } catch (Exception e) {
                // ignored
            }
        }
        return result;
    }

//    public static boolean startInstallWithReflection(Context context, String apkPath) {
//        installAppWithReflection(context, apkPath);
//        return true;
//    }
//
//    public static void installAppWithReflection(Context context, String path) {
//        String TAG = SilentInstall.class.getSimpleName();
//        try {
//            Log.e(TAG, "[installAppWithReflection][Strat]");
//            Log.e(TAG, "[installAppWithReflection][Path]" + path);
//            installWithSystem(context, path);
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage(), e);
//        }
//    }
//
//    public static void installWithSystem(Context context, String path) {
//        String TAG = SilentInstall.class.getSimpleName();
//        try {
//            Uri packageUri = Uri.fromFile(new File(path));
//            Log.e(TAG, "[installWithSystem][URI]" + packageUri);
//            PackageManager pkgManager = context.getPackageManager();
//            pkgManager.getClass().getMethod("installPackage", new Class[]{Uri.class, IPackageInstallObserver.class, Integer.TYPE, String.class}).invoke(pkgManager, new Object[]{packageUri, null, Integer.valueOf(2), ""});
//            Log.e(TAG, "[installWithSystem][END]");
//        } catch (NoSuchMethodException| InvocationTargetException | IllegalAccessException e){
//            Log.e(TAG, "silent install Error, " + e);
//            Toast.makeText(context, "APK更新失敗(Reflect Error)，可能沒有Sign System Key", Toast.LENGTH_SHORT).show();
//        }
//    }
}

