package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpservice.ICCPAidlInterface;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class VersionUpdateHelper implements APKDownloadTask.OnTaskFinished, APKDownloadTask.OnCancelled, APKDownloadTask.OnProgress {
    private String TAG = VersionUpdateHelper.class.getSimpleName();
    private Context context;
    private APKDownloadTask task = null;
    private String md5 = "";
    private String saveFileName = "";
    private String fileUrl = "";
    private boolean isCCPService = false;
    private boolean bindCCPService = false;
    ICCPAidlInterface iccpAidlInterface = null;
    ServiceConnection serviceConnection = null;

    public VersionUpdateHelper(Context context, ICCPAidlInterface iccpAidlInterface, ServiceConnection serviceConnection, boolean bindCCPService) {
        this.context = context;
        this.iccpAidlInterface = iccpAidlInterface;
        this.serviceConnection = serviceConnection;
        this.bindCCPService = bindCCPService;
    }

    public void downloadManager(String saveFileName, String fileUrl, String md5, boolean isCCPService) {
        this.md5 = md5;
        this.saveFileName = saveFileName;
        this.fileUrl = fileUrl;
        this.isCCPService = isCCPService;
        task = new APKDownloadTask(context, this, this, this, this.saveFileName, this.fileUrl);
        task.execute();
    }

    //安裝失敗會retry，還有下載失敗會retry，retry的時間在config中設定。
    @Override
    public void doSomething() {
        File rootFile = new File(context.getCacheDir(), "/download");

        Log.d(TAG, "Complete: complete " + saveFileName);
        Log.d(TAG, "folder: " + rootFile);

        // 變更資料夾&檔案存取權限 700=rwx-權限全開
        String filePath = rootFile.getAbsolutePath() +"/"+ saveFileName;
        String folderPath = rootFile.getAbsolutePath();
        try {
            Log.d(TAG, "chmod to 777....");
            Log.d(TAG, "chmod path: " + filePath);
            Runtime.getRuntime().exec(new String[]{"chmod", "777", filePath});
            Runtime.getRuntime().exec(new String[]{"chmod", "777", folderPath});


            Runtime.getRuntime().exec(new String[]{"su", "-c", "mount -o remount rw /system"} );
            Runtime.getRuntime().exec(new String[]{"chmod", "777", "/system/priv-app"});
//            try {
//                Process process = Runtime.getRuntime().exec("su");
//                DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
//                outputStream.writeBytes("mount -o rw,remount -t auto /system"+ "\n");
//                outputStream.flush();
//                outputStream.writeBytes("chmod 777 /system/priv-app" + "\n");
//                outputStream.flush();
//                outputStream.close();
//                process.destroy();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//            Process child = Runtime.getRuntime().exec(new String[] { "su"});
//            DataOutputStream stdin = new DataOutputStream(child.getOutputStream());
//            stdin.writeBytes("mount -o rw,remount -t auto /system");
//            stdin.writeBytes("chmod 777 /system/priv-app");
//            Runtime.getRuntime().exec(new String[]{"su"});
//            Runtime.getRuntime().exec(new String[]{"mount -o rw,remount -t auto /system"});
//            Runtime.getRuntime().exec(new String[]{"chmod", "777", "/system/priv-app"});
        } catch (IOException e) {}


        if (isCCPService) {
            // Silent Install
            if (SilentInstall.startInstall(filePath)) {
                Toast.makeText(context, "CCP Service下載&安裝成功", Toast.LENGTH_SHORT).show();

                // Start CCP Service.
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
            } else {
                Toast.makeText(context, "CCP Service下載失敗", Toast.LENGTH_SHORT).show();
                //shadow service need to retry.
                retryToDownload();
            }
        }
//        String[] children = rootFile.list();
//        for (int i = 0; i < children.length; i++) {
//            Log.d(TAG, children[i]);
//        }
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        File apkFile = new File(filePath);
//        if (md5 == null || md5.equals("") || md5.equals(Md5ChecksumHelper.calculateMD5(apkFile))) {
//            Uri uri;
//            //0714 新增Android 7.0版本判斷（sdk 24）
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                Log.d(TAG, "FileProvider: " + "RK3399");
//                uri = FileProvider.getUriForFile(context, "com.coretronic.iothubservice.fileprovider", apkFile);
//                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }else{
//                Log.d(TAG, "FileProvider: " + "Others");
//                uri = Uri.fromFile(apkFile);
//            }
//
//            i.setDataAndType(uri, "application/vnd.android.package-archive");
//            Log.d(TAG, "uri: " + uri);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.putExtra(Intent.EXTRA_RETURN_RESULT, true);
//            context.startActivity(i);
//        } else {
//            Log.d(TAG, "檔案驗證錯誤，請檢查檔案後重試");
//            Toast.makeText(context, "檔案驗證錯誤，請檢查檔案後重試", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void cancell() {
        Log.d(TAG, "cancell: cancell");
        Toast.makeText(context, "CCP Service下載失敗", Toast.LENGTH_SHORT).show();

        //CCP service need to retry.
        if (isCCPService){
            retryToDownload();
        }

    }

    private void retryToDownload(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                newDownloadTask();
            }
        }, Config.ccpserviceApkDownloadRetryMillisecond);
    }

    private void newDownloadTask(){
        task = null;
        task = new APKDownloadTask(context, this, this, this, this.saveFileName, this.fileUrl);
        task.execute();
    }

    private int pastPercent=0;
    @Override
    public void progress(Long... values) {
        float currentFloat = (values[0].floatValue() / values[1].floatValue()) * 100;
        int currentPercent = (int) currentFloat;
        Log.d(TAG, "progress: progress: " + currentPercent + "%");
        if (pastPercent >=100){
            pastPercent=0;
        }
    }
}
