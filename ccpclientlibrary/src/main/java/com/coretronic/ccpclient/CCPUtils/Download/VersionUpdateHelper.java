package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpservice.ICCPAidlInterface;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class VersionUpdateHelper implements APKDownloadTask.OnTaskFinished, APKDownloadTask.OnCancelled, APKDownloadTask.OnProgress, APKDownloadTask.OnError {
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
    private boolean isShadow = false;

    public VersionUpdateHelper(Context context, ICCPAidlInterface iccpAidlInterface, ServiceConnection serviceConnection, boolean bindCCPService) {
        this.context = context;
        this.iccpAidlInterface = iccpAidlInterface;
        this.serviceConnection = serviceConnection;
        this.bindCCPService = bindCCPService;
    }

    public VersionUpdateHelper(Context context) {
        this.context = context;
    }

    public void downloadManager(String saveFileName, String fileUrl, String md5, boolean isCCPService, boolean isShadow) {
        this.md5 = md5;
        this.saveFileName = saveFileName + System.currentTimeMillis()/1000+".apk";
        this.fileUrl = fileUrl;
        this.isCCPService = isCCPService;
        this.isShadow = isShadow;
        String savePath =context.getCacheDir().getPath() + Config.apkDownloadSavePath;
        task = new APKDownloadTask(context, this, this, this,this, savePath, this.saveFileName, this.fileUrl);
        task.setRetryPeriod(60000);
        // 先清除舊有的ccpservice與shadow安裝檔避免同檔名續傳錯誤
        task.deleteExistApk();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //安裝失敗會retry，還有下載失敗會retry，retry的時間在config中設定。
    @Override
    public void finish(String apkFilePath) {
        try {
            Config.installerLock.lock();
            Log.d("installerLock","lock");
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
            } catch (IOException e) {}


            if (isCCPService) {
                // Silent Install
                if (SilentInstall.startInstall(filePath)) {
//                try {
//                    Toast.makeText(context, "CCP Service下載&安裝成功", Toast.LENGTH_SHORT).show();
//                } catch (WindowManager.BadTokenException e) {
//                    e.printStackTrace();
//                }

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
                    Log.e(TAG,"CCP Service安裝失敗");
//                try {
//                    Toast.makeText(context, "CCP Service下載失敗", Toast.LENGTH_SHORT).show();
//                } catch (WindowManager.BadTokenException e){
//                    e.printStackTrace();
//                }
                    //shadow service need to retry.
                    Log.e(TAG, "retryToDownload: CCP Service安裝失敗");
                    retryToDownload();
                }
            }
            else if(isShadow) {
                if (SilentInstall.startInstall(filePath)) {
//                try {
//                    Toast.makeText(context, "Shadow下載&安裝成功", Toast.LENGTH_SHORT).show();
//                } catch (WindowManager.BadTokenException e){
//                    e.printStackTrace();
//                }
                    // Start CCP Service.
                    Intent intent = new Intent(Config.shadowStartAction);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    context.sendBroadcast(intent);
                } else {
                    Log.e(TAG,"Shadow安裝失敗");
//                try {
//                    Toast.makeText(context, "Shadow下載失敗", Toast.LENGTH_SHORT).show();
//                } catch (WindowManager.BadTokenException e){
//                    e.printStackTrace();
//                }
                    //shadow service need to retry.
                    Log.e(TAG, "retryToDownload: Shadow安裝失敗");
                    retryToDownload();
                }
            }
        } finally {
            Config.installerLock.unlock();
            Log.d("installerLock","unlock");
        }

    }

    @Override
    public void cancel() {
        Log.e(TAG, "cancel: cancel");
//        try {
//            Toast.makeText(context, "CCP Service下載失敗", Toast.LENGTH_SHORT).show();
//        } catch (WindowManager.BadTokenException e){
//            e.printStackTrace();
//        }

        //CCP service need to retry.
        if (isCCPService){
            Log.e(TAG, "retryToDownload: cancel");
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
        Log.e(TAG, "into newDownloadTask");
        task = null;
        String savePath =context.getCacheDir().getPath() + Config.apkDownloadSavePath;
        this.saveFileName = saveFileName + System.currentTimeMillis()/1000+".apk";
        task = new APKDownloadTask(context, this, this, this,this, savePath, this.saveFileName, this.fileUrl);
        task.deleteExistApk();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private int pastPercent=0;
    @Override
    public void progress(Long... values) {
        float currentFloat = (values[0].floatValue() / values[1].floatValue()) * 100;
        int currentPercent = (int) currentFloat;
        if(currentPercent>pastPercent) {
            pastPercent = currentPercent;
            Log.d(TAG, "progress: " + saveFileName + " " + currentPercent + "%");
        }
    }

    @Override
    public void error(String errorMsg) {
        Log.e(TAG, "error: " + errorMsg);
    }
}
