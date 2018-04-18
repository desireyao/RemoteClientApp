package com.yaoh.picdiff;

import android.app.Application;

import com.yaoh.picdiff.tools.LogTool;

import static com.yaoh.picdiff.Constants.BASE_DIR_PATH;

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
