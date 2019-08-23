package com.coretronic.ccpclientlibrary.CCPUtils;
/**
 * Created by Jones Lin on 2019-08-08.
 */
public class Config {
    public static String ccpservicePackageName = "com.coretronic.ccpservice";
    public static String apkDownloadSavePath = "/download/";
    public static String ccpserviceStartAction = "coretronic.intent.action.iot.service.START_BY_SHADOW";
    public static String ccpserviceApkDownloadPath = "https://ftp.coretronic.com/dl/coretronicnote/ccpservice/ccpservice.apk";
    public static Long ccpserviceApkDownloadRetryMillisecond = 600 * 1000L;
    public static boolean isBindService = false;
}
