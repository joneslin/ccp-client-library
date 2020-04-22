package com.coretronic.ccpclient.CCPUtils.Download;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by Jones Lin on 2019-08-19.
 */
public class SilentInstall {

    public static boolean startInstall(String apkPath) {
        final String TAG = SilentInstall.class.getSimpleName();
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        final StringBuilder successMsg = new StringBuilder();
        final StringBuilder errorMsg = new StringBuilder();
        final Process process;
        try {
            // 申请 su 權限
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 標準安裝：pm install 命令
            String command = "pm install -r " + apkPath + "\n";
            dataOutputStream.write(command.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    try {
                        while ((line = errorStream.readLine()) != null) {
                            errorMsg.append(line);
                        }
                        Log.e(TAG, "silent install error message: " + errorMsg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (errorStream != null) {
                            try {
                                errorStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader successStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    // 讀取命令執行結果
                    String line;
                    try {
                        while ((line = successStream.readLine()) != null) {
                            successMsg.append(line);
                        }
                        Log.e(TAG, "silent install success message: " + successMsg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (successStream != null) {
                            try {
                                successStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();

            process.waitFor();

            // 如果執行結果中包含 Failure 字樣就認為是操作失敗，否則就認為安裝成功
            if (!(errorMsg.toString().contains("Failure") || successMsg.toString().contains("Failure") || errorMsg.toString().contains("Killed"))) {
                result = true;
            }

            if (process != null) {
                process.destroy();
            }

            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
        }
        return result;
    }
}

