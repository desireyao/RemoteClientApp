package com.yaoh.remoteclient;

import android.app.Application;

import com.yaoh.remoteclient.tools.LogTool;

import static com.yaoh.remoteclient.Constants.BASE_DIR_PATH;

/**
 * Created by yaoh on 2018/4/4.
 */

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        LogTool.init(BASE_DIR_PATH, 7, true);
    }

    public static App getApp() {
        return app;
    }
}
