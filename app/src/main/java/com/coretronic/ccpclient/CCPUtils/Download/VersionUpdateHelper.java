package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpservice.ICCPAidlInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.PACKAGE_ADDED");
//        filter.addDataScheme("package");
//        context.registerReceiver(broadcastReceiver, filter);
    }

    public VersionUpdateHelper(Context context) {
        this.context = context;
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.PACKAGE_ADDED");
//        filter.addDataScheme("package");
//        context.registerReceiver(broadcastReceiverForShadow, filter);
    }

//    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
//                String packageName = intent.getDataString();
//                Log.e(TAG, "安装了:" + packageName);
//
//                if (isCCPService) {
//                    // Silent Install
//                    Toast.makeText(context, "CCP Service下載&安裝成功", Toast.LENGTH_SHORT).show();
//                    // Start CCP Service.
//                    Intent intentToService = new Intent(Config.ccpserviceStartAction);
//                    intentToService.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                    context.sendBroadcast(intentToService);
//                    if (bindCCPService) {
//                        // Bind AIDL.
//                        if (iccpAidlInterface == null) {
//                            Intent it = new Intent();
//                            //service action.
//                            it.setAction("coretronic.intent.action.aidl");
//                            //service package name.
//                            it.setPackage("com.coretronic.ccpservice");
//                            context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE);
//                            Config.isBindService = true;
//                        }
//                    }
//                    context.unregisterReceiver(broadcastReceiver);
//                }
//            }
//        }
//    };

//    BroadcastReceiver broadcastReceiverForShadow = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
//                String packageName = intent.getDataString();
//                Log.e(TAG, "安装了:" + packageName);
//
//                if (!isCCPService) {
//                    Toast.makeText(context, "Shadow下載&安裝成功", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "*****starting Shadow Service");
//                    Intent intentToShadow = new Intent(Config.shadowStartAction);
//                    intentToShadow.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                    context.sendBroadcast(intentToShadow);
//                }
//            }
//        }
//    };

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
        File rootFile = new File(Environment.getExternalStorageDirectory(), "/Download");

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


        try {
            Uri fileUri = Uri.fromFile(new File(filePath));
            InputStream is = context.getContentResolver().openInputStream(fileUri);
            SilentInstall.startInstall(context, filePath);
        } catch (IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG,e.getMessage());
        }

    }

    @Override
    public void cancell() {
        Log.d(TAG, "cancell: cancell");
//        Toast.makeText(context, "CCP Service下載失敗", Toast.LENGTH_SHORT).show();
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
