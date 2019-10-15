package com.coretronic.ccpclient.CCPUtils.Download;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.IBinder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;


/**
 * Created by Jones Lin on 2019-08-19.
 */
public class SilentInstall {

//    public static boolean startInstall(String apkPath) {
//        String TAG = SilentInstall.class.getSimpleName();
//        boolean result = false;
//        DataOutputStream dataOutputStream = null;
//        BufferedReader errorStream = null;
//        BufferedReader successStream = null;
//        Process process = null;
//        try {
//            // 申请 su 權限
//            process = Runtime.getRuntime().exec("su");
//            dataOutputStream = new DataOutputStream(process.getOutputStream());
//
//            // 標準安裝：pm install 命令
//            String command = "pm install -r " + apkPath + "\n";
//
//            // 安裝至system/priv-app，成為系統app
////            dataOutputStream.writeBytes("mount -o rw,remount -t auto /system"+ "\n");
////            dataOutputStream.flush();
////            dataOutputStream.writeBytes("chmod 777 /system/priv-app" + "\n");
////            dataOutputStream.flush();
////            String command = "mv " + apkPath + " /system/priv-app" + "\n";
//
//            dataOutputStream.write(command.getBytes(Charset.forName("UTF-8")));
//            dataOutputStream.writeBytes("exit\n");
//            dataOutputStream.flush();
//            process.waitFor();
//            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            StringBuilder errorMsg = new StringBuilder();
//            String line;
//            while ((line = errorStream.readLine()) != null) {
//                errorMsg.append(line);
//            }
//            Log.d(TAG, "silent install error message: " + errorMsg);
//            StringBuilder successMsg = new StringBuilder();
//            successStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            // 讀取命令執行結果
//            while ((line = successStream.readLine()) != null) {
//                successMsg.append(line);
//            }
//            Log.d(TAG, "silent install success message: " + successMsg);
//            // 如果執行結果中包含 Failure 字樣就認為是操作失敗，否則就認為安裝成功
//            if (!(errorMsg.toString().contains("Failure") || successMsg.toString().contains("Failure"))) {
//                result = true;
//            }
//        } catch (Exception e) {
//            Log.d(TAG, "Exception: " + e);
//        } finally {
//            try {
//                if (process != null) {
//                    process.destroy();
//                }
//                if (dataOutputStream != null) {
//                    dataOutputStream.close();
//                }
//                if (errorStream != null) {
//                    errorStream.close();
//                }
//                if (successStream != null) {
//                    successStream.close();
//                }
//            } catch (Exception e) {
//                // ignored
//            }
//        }
//        return result;
//    }

    public static boolean startInstall(Context context, InputStream in, String packageName)
            throws IOException {

        File fileName = new File(packageName);

//        Class<?> pmClz = packageManager.getClass();
//        try {
//            if (Build.VERSION.SDK_INT >= 21) {
//                Class<?> aClass = Class.forName("android.app.PackageInstallObserver");
//                Constructor<?> constructor = aClass.getDeclaredConstructor();
//                constructor.setAccessible(true);
//                Object installObserver = constructor.newInstance();
//                Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, aClass, int.class, String.class);
//                method.setAccessible(true);
//                method.invoke(packageManager, Uri.fromFile(new File(apkPath)), installObserver, 2, null);
//            } else {
//                Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class);
//                method.setAccessible(true);
//                method.invoke(packageManager, Uri.fromFile(new File(apkPath)), null, 2, null);
//            }
//            return true;
//        } catch (Exception e) {
//        }



        try {

            Uri uri = Uri.fromFile(fileName);
            // 通过Java反射机制获取android.os.ServiceManager
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method = clazz.getMethod("getService", String.class);
            IBinder iBinder = (IBinder) method.invoke(null, "package");
            IPackageManager ipm = IPackageManager.Stub.asInterface(iBinder);
            @SuppressWarnings("deprecation")
            VerificationParams verificationParams = new VerificationParams(null, null, null, VerificationParams.NO_UID, null);
            // 执行安装（方法及详细参数，可能因不同系统而异）
            ipm.installPackage(fileName, new PackageInstallObserver(), 2, null, verificationParams, "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
//        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
//                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
//        params.setAppPackageName(packageName);
        // set params
//        int sessionId = packageInstaller.createSession(params);
//        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
//        OutputStream out = session.openWrite("my_app_session", 0, -1);
//        byte[] buffer = new byte[65536];
//        int c;
//        while ((c = in.read(buffer)) != -1) {
//            out.write(buffer, 0, c);
//        }
//        session.fsync(out);
//        in.close();
//        out.close();
//
//        // fake intent
//        IntentSender statusReceiver = null;
//        Intent intent = new Intent(context, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
//                1337111117, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        session.commit(pendingIntent.getIntentSender());
//        session.close();

//        session.commit(createIntentSender(context, sessionId));

//        android.content.pm.PackageInstaller.Session session = null;
//        try {
//            int sessionId = packageInstaller.createSession(params);
//            session = packageInstaller.openSession(sessionId);
//            OutputStream out = session.openWrite("ccpservice", 0, -1);
//            byte buffer[] = new byte[1024];
//            int length;
//            int count = 0;
//            while ((length = in.read(buffer)) != -1) {
//                out.write(buffer, 0, length);
//                count += length;
//            }
//            session.fsync(out);
//            out.close();
//
//            Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED);
//
//            session.commit(PendingIntent.getBroadcast(context, sessionId,
//                    intent, PendingIntent.FLAG_UPDATE_CURRENT).getIntentSender());
//        } finally {
//            if (session != null) {
//                session.close();
//            }
//        }
        return true;
    }



    public static final String ACTION_INSTALL_COMPLETE
            = "com.afwsamples.testdpc.INSTALL_COMPLETE";

    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(ACTION_INSTALL_COMPLETE),
                0);
        return pendingIntent.getIntentSender();
    }
}
