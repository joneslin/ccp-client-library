// ICCPAidlCallback.aidl
package com.coretronic.ccpservice;

// Declare any non-default types here with import statements
// Created by Jones
oneway interface ICCPAidlCallback {

    void serviceInt(int value);

    void serviceString(String value);

    void ccpServiceReady(String messageCode);

    void apkReadyToInstall(String packageName, String folderPath, String fileName);

    void firmwareReadyToInstall(String title, String folderPath, String fileName);

}
