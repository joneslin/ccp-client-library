package com.coretronic.ccpclient.CCPUtils.Download;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;

import com.coretronic.ccpclient.CCPUtils.Config;
import com.coretronic.ccpclient.CCPUtils.Router.RouterAzure;

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
    private String fileName;
    private String fileUrl;
    private String localFilePath = "";
    private boolean downloadInterrupt = false;

    public APKDownloadTask(Context context, OnTaskFinished onTaskFinished, OnCancelled onCancelled, OnProgress onProgress, String fileName, String fileUrl) {
        this.onTaskFinished = onTaskFinished;
        this.onCancelled = onCancelled;
        this.onProgress = onProgress;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.context = context;
        localFilePath = context.getCacheDir().getPath() + Config.apkDownloadSavePath;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "preExecute = " + "yes");
    }

    @Override
    protected void onPostExecute(List<File> files) {
        if (!downloadInterrupt) {
            onTaskFinished.finish(localFilePath+fileName);
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

        OkHttpClient httpClient = RouterAzure.getUnsafeOkHttpClient();
        Call call = httpClient.newCall(new Request.Builder().url(fileUrl).get().build());
        try {
            Response response = call.execute();
            if (response.code() == 200) {
                Log.d(TAG, "200 OK");
                InputStream inputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    byte[] buff = new byte[1024 * 4];
                    long downloaded = 0;
                    long target = response.body().contentLength();

                    File compressedFile = new File(localFilePath, fileName);
                    OutputStream outputStream = new FileOutputStream(compressedFile);
                    publishProgress(0L, target);
                    while (true) {
                        int readed = inputStream.read(buff);
                        if (readed == -1) {
                            break;
                        }
                        // write buff to file
                        outputStream.write(buff, 0, readed);
                        downloaded += readed;
                        publishProgress(downloaded, target);

                        if (isCancelled()) {
                            downloadInterrupt = true;
                            break;
//                                    return null; //中途取消
                        }
                    }
                    outputStream.close();
                } catch (IOException ignore) {
                    Log.d(TAG, "IOException");
                    downloadInterrupt = true;
                    return null;  //例外處理
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } else {
                Log.d(TAG, "no connection");
                downloadInterrupt = true;
                return null;  //無法連線
            }
        } catch (IOException e) {
            downloadInterrupt = true;
            e.printStackTrace();
            return null;
        }
        return null;
    }

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
}
