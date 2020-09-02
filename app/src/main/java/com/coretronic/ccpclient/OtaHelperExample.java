package com.coretronic.ccpclient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpclient.CCPUtils.Download.APKDownloadTask;
import com.coretronic.ccpclient.CCPUtils.Download.PackageHelper;
import com.coretronic.ccpclient.CCPUtils.Download.SilentInstall;
import com.coretronic.ccpclient.CCPUtils.Model.LatestOTAByDeviceGson.SoftwaresBean;
import com.coretronic.ccpclient.CCPUtils.Model.LatestOTAByDeviceGson.FirmwareBean;
import com.coretronic.ccpservice.ICCPAidlInterface;

public class OtaHelperExample implements APKDownloadTask.OnCancelled, APKDownloadTask.OnProgress, APKDownloadTask.OnTaskFinished, APKDownloadTask.OnError {
    private Context context;
    private APKDownloadTask task = null;
    private ICCPAidlInterface iccpAidlInterface;
    private boolean isFirmware;
    private String title;
    private String newVersion;
    private String localVersion;
    public OtaHelperExample(Context context) {
        this.context = context;
    }

    public void startUpdateFiirmware(ICCPAidlInterface iccpAidlInterface, FirmwareBean firmware, String localVersion)
    {
        this.isFirmware = true;
        this.iccpAidlInterface = iccpAidlInterface;
        this.title = firmware.getTitle();
        this.newVersion = firmware.getVersion();
        this.localVersion = localVersion;

        try {
            this.iccpAidlInterface.sendFirmwareOtaStatus(title,"applying", localVersion, newVersion,"");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        String savePath = context.getCacheDir().getPath() + Config.apkDownloadSavePath;
        task = new APKDownloadTask(context, this, this, this, this,savePath, firmware.getName(), firmware.getUri());
//        if(!task.isApkExist(currentSoftware.getChecksum()))
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void startUpdateSoftware(ICCPAidlInterface iccpAidlInterface, SoftwaresBean software, String localVersion)
    {
        this.isFirmware = false;
        this.iccpAidlInterface = iccpAidlInterface;
        this.title = software.getTitle();
        this.newVersion = software.getVersion();
        this.localVersion = localVersion;

        try {
            this.iccpAidlInterface.sendOtaStatus(title,"applying", localVersion, newVersion,"");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        String savePath = "/sdcard/Download/";
        task = new APKDownloadTask(context, this, this, this, this,savePath, software.getPackageName(), software.getUri());
//        if(!task.isApkExist(currentSoftware.getChecksum()))
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void cancel() {
        Log.d("OtaHelper",  "download cancelled");
    }

    private  int lastProgress=0;
    @Override
    public void progress(Long... values) {
        float currentFloat = (values[0].floatValue() / values[1].floatValue()) * 100;
        int currentPercent = (int) currentFloat;
        if(currentPercent!=lastProgress)
            Log.d("OtaHelper", title + " progress: progress: " + currentPercent + "%");
        lastProgress = currentPercent;
    }

    @Override
    public void finish(String apkFilePath) {
        Log.d("OtaHelper",  "download finished: " + apkFilePath);
        // TODO: 安裝apk
        if(isFirmware) {
            // TODO: 執行安裝韌體，並使用sendFirmwareOtaStatus回傳版本更新狀態
//            try {
//                if(SilentInstall.startInstall(apkFilePath))
//                {
//                    iccpAidlInterface.sendFirmwareOtaStatus(currentSoftware.getTitle(),"current", localVersion, "");
//                }
//                else
//                {
//                    iccpAidlInterface.sendFirmwareOtaStatus(currentSoftware.getTitle(),"error", localVersion, currentSoftware.getVersion());
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        } else {
            // TODO: 執行安裝軟體
            try {
                if(SilentInstall.startInstall(apkFilePath))
                {
                    // TODO: 在MainActivity的BroadcastReceiver處理安裝成功的事件
                }
                else
                {
                    iccpAidlInterface.sendOtaStatus(title,"error", localVersion, newVersion,"");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void error(String errorMsg) {

    }
}
