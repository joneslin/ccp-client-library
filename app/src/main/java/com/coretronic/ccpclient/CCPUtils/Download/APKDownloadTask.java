package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;

import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpclient.CCPUtils.Router.RouterAzure;

import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APKDownloadTask extends AsyncTask<Void, Long, List<File>> {
    private String TAG = APKDownloadTask.class.getSimpleName();
    private Context context;
    private OnTaskFinished onTaskFinished;
    private OnCancelled onCancelled;
    private OnProgress onProgress;
    private OnError onError;
    private String fileName;
    private String fileUrl;
    private String localFilePath = "";
    private boolean downloadInterrupt = false;

    private OkHttpClient httpClient = null;
    private long targetSize = 0L;
    private RandomAccessFile savedFile = null;
    private boolean needsRetry = true;
    private long retryPeriod = 5000;

    public APKDownloadTask(Context context, OnTaskFinished onTaskFinished, OnCancelled onCancelled, OnProgress onProgress, OnError onError, String savePath, String fileName, String fileUrl) {
        this.onTaskFinished = onTaskFinished;
        this.onCancelled = onCancelled;
        this.onProgress = onProgress;
        this.onError = onError;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.context = context;
//        localFilePath = context.getCacheDir().getPath() + Config.apkDownloadSavePath;
        localFilePath = savePath;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "preExecute = " + "yes");
    }

    @Override
    protected void onPostExecute(List<File> files) {
        if (!downloadInterrupt) {
            onTaskFinished.finish(localFilePath+"/"+fileName);
            Log.d(TAG, "onPostExecute = " + "download complete");
        } else {
            Log.d(TAG, "onPostExecute = " + "download cancel");
            onCancelled.cancel();
        }
    }

    @Override
    protected void onCancelled() {
        onCancelled.cancel();

    }

    @Override
    protected List<File> doInBackground(Void... params) {
        long freeSize = storage_free();
        Log.d(TAG, "Storage SIZE:" + freeSize);

        //當內存小於1G，即清理所有下載的媒體檔
        if (freeSize < 1000000000) {
            clearApplicationData();
            Log.d(TAG, "Clear All Media Data");
        }

        File folder = new File(localFilePath);
        if (folder.isDirectory()) {
        } else {
            folder.mkdirs();
        }

        if (!isCancelled()) {
            targetSize = getContentLength(fileUrl);
            httpClient = RouterAzure.getUnsafeOkHttpClient();
            savedFile = null;
            needsRetry = true;



            //through interface to show current download count and total.
//            oncount.currentCount(i + 1, fileUrlArrayList.size(), fileName);
//            progress.getCurrentFileName(fileName);

            Log.d(TAG, "willDownloadFileName: " + fileName);
            //init file
            File file = new File(localFilePath + "/" +fileName);

            //若檔案存在，且預計下載的檔案大小與Local端的檔案大小一致，就不進行確認與下載
            if (file.exists() && targetSize > 0 && file.length() == targetSize) {
                Log.d(TAG, fileName + ": already download and file size is right");

            } else {
                while (needsRetry) {
                    try {
                        Log.d(TAG, "Start downloading");
                        Call call = httpClient.newCall(new Request.Builder().
                                header("Range", "bytes=" + file.length() + "-" + targetSize).  // 續傳專案參數
                                url(fileUrl).get().build());

                        Response response = call.execute();
                        Log.d(TAG, "ResponseCode: " + response.code());

                        // 訪問續傳，成功回傳206
                        if (response.code() == 206 && targetSize != 0) {
                            Log.d(TAG, "ResponseCode: 206, " + "bytes=" + file.length());
                            InputStream inputStream = response.body().byteStream();
                            byte[] buff = new byte[1024];
                            long downloaded = 0;

                            // 4. 開始下載
                            publishProgress(0L, targetSize);
                            savedFile = new RandomAccessFile(file, "rw");  //開始訪問指定的文件
                            savedFile.seek(file.length());  //跳過已經下載的文件長度
                            while (true) {
                                int readed = inputStream.read(buff);
                                if (readed == -1) {
                                    break;
                                }
                                // 5. Write buff to file
                                savedFile.write(buff, 0, readed);
                                downloaded += readed;
                                publishProgress(savedFile.length(), targetSize);
                                if (isCancelled()) {
                                    Log.d(TAG, "TaskDownload:" + "中途取消");
                                    downloadInterrupt = true;
                                    return null; //中途取消
                                }
                            }

                            savedFile.close();
                            if (inputStream != null) {
                                inputStream.close();
                            }

                            Log.d(TAG, fileName + " Download ok");
                            needsRetry = false;
                        }
                        // **** if response is not equal to 206 (can't support continue download), the response will return 200 when internet is ok. ****
                        else if (response.code() == 200 && targetSize != 0) {
                            Log.d(TAG, "ResponseCode: 200");
                            InputStream inputStream = response.body().byteStream();
                            byte[] buff = new byte[1024];
                            long downloaded = 0;

                            // 4. 開始下載
                            File compressedFile = new File(localFilePath, fileName);
                            OutputStream outputStream = new FileOutputStream(compressedFile);
                            publishProgress(0L, targetSize);

                            while (true) {
                                int readed = inputStream.read(buff);
                                if (readed == -1) {
                                    break;
                                }
                                // write buff to file
                                outputStream.write(buff, 0, readed);
                                downloaded += readed;
                                publishProgress(downloaded, targetSize);

                                if (isCancelled()) {
                                    Log.d(TAG, "TaskDownload:" + "中途取消");
                                    downloadInterrupt = true;
                                    return null; //中途取消
                                }
                            }
                            outputStream.flush();
                            outputStream.close();
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            downloadInterrupt = false;
                            Log.d(TAG, fileName + " Download ok");
                            needsRetry = false;
                            return null;
                        } else if (response.code() == 416 && targetSize != 0) {
                            // response code = 416 is already download.
                            needsRetry = false;
                            downloadInterrupt = false;
                        } else {
                            downloadInterrupt = false;
                            onError.error("ResponseCode: "+response.code());
//                            return null;  //無法連線
                        }
                    }
                    catch (Exception e) {
                        Log.e(TAG, "download exception"+e.getMessage());
                        downloadInterrupt = false;
//                        e.printStackTrace();
                        onError.error(e.getMessage());

                    }
                    Log.e(TAG, "Sleep" + retryPeriod + "milliseconds ...");
                    try {
                        Thread.sleep(retryPeriod);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
//            break;
        }
        return null;
    }
//    protected List<File> doInBackground(Void... params) {
//        long freeSize = storage_free();
//        Log.d(TAG, "Storage SIZE:" + freeSize);
//
//        //當內存小於1G，即清理所有下載的媒體檔
//        if (freeSize < 1000000000) {
//            clearApplicationData();
//            Log.d(TAG, "Clear All Media Data");
//        }
//
//        File folder = new File(localFilePath);
//        if (folder.isDirectory()) {
//        } else {
//            folder.mkdirs();
//        }
//
//        OkHttpClient httpClient = RouterAzure.getUnsafeOkHttpClient();
//        Call call = httpClient.newCall(new Request.Builder().url(fileUrl).get().build());
//        try {
//            Response response = call.execute();
//            if (response.code() == 200) {
//                Log.d(TAG, "200 OK");
//                InputStream inputStream = null;
//                try {
//                    inputStream = response.body().byteStream();
//                    byte[] buff = new byte[1024 * 4];
//                    long downloaded = 0;
//                    long target = response.body().contentLength();
//
//                    File compressedFile = new File(localFilePath, fileName);
//                    OutputStream outputStream = new FileOutputStream(compressedFile);
//                    publishProgress(0L, target);
//                    while (true) {
//                        int readed = inputStream.read(buff);
//                        if (readed == -1) {
//                            break;
//                        }
//                        // write buff to file
//                        outputStream.write(buff, 0, readed);
//                        downloaded += readed;
//                        publishProgress(downloaded, target);
//
//                        if (isCancelled()) {
//                            downloadInterrupt = true;
//                            break;
////                                    return null; //中途取消
//                        }
//                    }
//                    outputStream.close();
//                } catch (IOException ignore) {
//                    Log.d(TAG, "IOException");
//                    downloadInterrupt = true;
//                    return null;  //例外處理
//                } finally {
//                    if (inputStream != null) {
//                        inputStream.close();
//                    }
//                }
//            } else {
//                Log.d(TAG, "no connection");
//                downloadInterrupt = true;
//                return null;  //無法連線
//            }
//        } catch (IOException e) {
//            downloadInterrupt = true;
//            e.printStackTrace();
//            return null;
//        }
//        return null;
//    }

    @Override
    protected void onProgressUpdate(Long... values) {
        onProgress.progress(values);
    }

    public interface OnTaskFinished {
        void finish(String apkFilePath);
    }

    public interface OnCancelled {
        void cancel();
    }

    public interface OnProgress {
        void progress(Long... values);
    }

    public interface OnError {
        void error(String errorMsg);
    }

    public long storage_free() {
        File path = android.os.Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        int availBlocks = stat.getAvailableBlocks();
        int blockSize = stat.getBlockSize();
        long free_memory = (long) availBlocks * (long) blockSize;
        return free_memory;
    }

    public void clearApplicationData() {
        File folder = new File(localFilePath);
        if (folder.exists()) {
            String[] children = folder.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(folder, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public boolean isApkExist(String md5Origin) {
        File folder = new File(localFilePath);
        try {
//            String md5Origin  = "";//original file's md5 checksum
            String filePath   = localFilePath+"/"+fileName; //fill with the real file path name
            Log.d("isApkExist",filePath);
            FileInputStream fis   = new FileInputStream(filePath);
            String md5Checksum    = md5(fis);
            Log.d("MD5",md5Checksum);
            if (md5Checksum.equals(md5Origin)) {
                //file is valid
                return true;
            }
        } catch (Exception e) {
            Log.e("md5",e.getMessage());
        }
        return false;
    }
    private static char[] hexDigits = "0123456789abcdef".toCharArray();

    public static String md5(InputStream is) throws IOException {
        String md5 = "";

        try {
            byte[] bytes = new byte[4096];
            int read = 0;
            MessageDigest digest = MessageDigest.getInstance("MD5");

            while ((read = is.read(bytes)) != -1) {
                digest.update(bytes, 0, read);
            }

            byte[] messageDigest = digest.digest();

            StringBuilder sb = new StringBuilder(32);

            for (byte b : messageDigest) {
                sb.append(hexDigits[(b >> 4) & 0x0f]);
                sb.append(hexDigits[b & 0x0f]);
            }

            md5 = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return md5;
    }

    public long getContentLength(String url) {
        Log.d(TAG, "contentLength url:" + url);

        try {
            httpClient = RouterAzure.getUnsafeOkHttpClient();
            Call call = httpClient.newCall(new Request.Builder().url(url).get().build());
            Response response = call.execute();
            if (response.code() == 200) {
                Log.d(TAG, "contentLength:" + response.body().contentLength());
                return response.body().contentLength();
            } else {
                return 0;
            }

        } catch (IOException e) {
            downloadInterrupt = true;
            e.printStackTrace();
            return 0;
        }
    }

    public void setRetryPeriod(long millis) {
        retryPeriod = millis;
    }
}
