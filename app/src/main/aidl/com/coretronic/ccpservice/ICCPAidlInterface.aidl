// ICCPAidlInterface.aidl
package com.coretronic.ccpservice;

import com.coretronic.ccpservice.ICCPAidlCallback;

// Declare any non-default types here with import statements
// Created by Jones
interface ICCPAidlInterface {

    void registerCallback(ICCPAidlCallback cb);

    void unregisterCallback(ICCPAidlCallback cb);

    String sendString(String data, String msgId);

    String sendInt(int data);

    void sendRegisterInfo(String deviceId, String tenantId, String productId);

    String sendOtaStatus(String packageName, String otaStatus, String localVersion, String updateVersion, String msg, String configuration);

    String sendFirmwareOtaStatus(String title, String otaStatus, String localVersion, String updateVersion, String msg, String configuration);

    void requestOtaInfo();

    void reportSystemInfo(String firmwareVersion);

    void sendValidationInfo(String deviceId, String guid, String secretKey, String connectionString);
}
