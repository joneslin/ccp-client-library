package com.coretronic.ccpclient.CCPUtils.Example;

import android.content.Context;
import android.os.Environment;

import com.coretronic.ccpclient.CCPUtils.Logger.DiskLogHandler;
import com.coretronic.ccpclient.CCPUtils.Logger.User;
import com.google.gson.Gson;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.DiskLogStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

/**
 * Created by Jones Lin on 2019-09-17.
 */
public class LoggerExample {

    public void saveLogToFile(Context context){
        //標準sdcard/logger路徑、logs_檔名、500k檔案大小
//        FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
//                .tag(this.getPackageName())
//                .build();

        // 客製化路徑、檔名、檔案大小
        FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
                .tag(context.getPackageName())
                .logStrategy(new DiskLogStrategy(new DiskLogHandler(Environment.getExternalStorageDirectory().getAbsolutePath()+"/logger/", "log", 1000*1000)))
                .build();

        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));
        for (int i=0; i<=20; i++) {
            User user = new User();
            user.setAccount("這是帳號");
            user.setMobile("這是電話");
            Logger.d(new Gson().toJson(user));
        }
    }
}
