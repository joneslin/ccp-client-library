package com.coretronic.ccpclient;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.coretronic.ccpclient.CCPUtils.Download.APKDownloadTask;
import com.coretronic.ccpclient.CCPUtils.Download.PackageHelper;
import com.coretronic.ccpclient.CCPUtils.Download.SilentInstall;
import com.coretronic.ccpclient.CCPUtils.Model.Software;
import com.coretronic.ccpservice.ICCPAidlInterface;

public class OtaHelperExample implements APKDownloadTask.OnCancelled, APKDownloadTask.OnProgress, APKDownloadTask.OnTaskFinished, APKDownloadTask.OnError {
    private Context context;
    private APKDownloadTask task = null;
    private Software currentSoftware = null;
    private ICCPAidlInterface iccpAidlInterface;
    private boolean isFirmware;
    public OtaHelperExample(Context context) {
        this.context = context;
    }

    public void startUpdate(ICCPAidlInterface iccpAidlInterface, Software software, boolean isFirmware)
    {
        this.isFirmware = isFirmware;
        this.iccpAidlInterface = iccpAidlInterface;
        this.currentSoftware = software;

        if(isFirmware) {
            // TODO: 取得firmware版本
            String localVersion = "unknown";
            try {
                iccpAidlInterface.sendFirmwareOtaStatus(currentSoftware.getTitle(),"applying", localVersion, currentSoftware.getVersion());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            String localVersion = PackageHelper.getVersionName(currentSoftware.getTitle(), context);
            try {
                iccpAidlInterface.sendOtaStatus(currentSoftware.getTitle(),"applying", localVersion, currentSoftware.getVersion());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
//        String savePath =context.getCacheDir().getPath() + "/download/";
        String savePath = "/sdcard/Download/";
        task = new APKDownloadTask(context, this, this, this, this,savePath, software.getName(), software.getUri());
//        if(!task.isApkExist(currentSoftware.getChecksum()))
            task.execute();
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
            Log.d("OtaHelper", currentSoftware.getTitle() + " progress: progress: " + currentPercent + "%");
        lastProgress = currentPercent;
    }

    @Override
    public void finish(String apkFilePath) {
        Log.d("OtaHelper",  "download finished: " + apkFilePath);
        // TODO: 安裝apk
        if(isFirmware) {
            // TODO: 取得firmware版本
            String localVersion = "unknown";
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
            // TODO: 執行安裝軟體，並使用sendOtaStatus回傳版本更新狀態
            try {
                if(SilentInstall.startInstall(apkFilePath))
                {
                    iccpAidlInterface.sendOtaStatus(currentSoftware.getTitle(),"current", currentSoftware.getVersion(), "");
                }
                else
                {
                    iccpAidlInterface.sendOtaStatus(currentSoftware.getTitle(),"error", PackageHelper.getVersionName(currentSoftware.getTitle(), context), currentSoftware.getVersion());
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
