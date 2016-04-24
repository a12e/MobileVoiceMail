package com.alcatel.mobilevoicemail;

import android.app.Application;
import android.content.Context;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

public class App extends Application {
    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    @Override
    public void onTerminate() {
        OpenTouchClient.getInstance().logout();
        super.onTerminate();
    }
}