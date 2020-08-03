// ICCPAidlCallback.aidl
package com.coretronic.ccpservice;

// Declare any non-default types here with import statements
// Created by Jones
oneway interface ICCPAidlCallback {

    void serviceInt(int value);

    void serviceString(String value, String msgId);

    void ccpServiceReady(String messageCode);

    void getOtaInfo(String latestOTAByDeviceStr);

    void ccpServiceValidated(String guid, String secretKey);

    void ccpServiceValidationResult(int statusCode);

}
