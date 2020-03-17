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

    String sendControlPackageNameArray(inout List<String> data);

    void sendRegisterInfo(String deviceId, String tenantId);

    void sendValidationInfo(String deviceId, String guid, String secretKey, String connectionString);
}
