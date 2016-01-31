package com.alcatel.mobilevoicemail;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DropboxClient {
    private static DropboxClient instance = null;

    final static private String APP_KEY = "ydma9rv0xm4wf4d";
    final static private String APP_SECRET = "ei7oouuh13yfp4l";
    final static private String APP_OATOKEN = "";
    private DropboxAPI<AndroidAuthSession> mDBApi = null;

    private DropboxClient() {
        AppKeyPair keyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        mDBApi = new DropboxAPI<>(new AndroidAuthSession(keyPair, APP_OATOKEN));
    }

    public static DropboxClient getInstance() {
        if(instance == null) {
            instance = new DropboxClient();
        }
        return instance;
    }

    public DropboxAPI<AndroidAuthSession> getApi() {
        return mDBApi;
    }

    public void startOAuth2Authentication(Context context) {
        mDBApi.getSession().startOAuth2Authentication(context);
    }

    private class UploadFileTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            if(params.length != 1) {
                throw new IllegalArgumentException("Only one paramter please");
            }

            try {
                Log.i(getClass().getSimpleName(), "Using dropbox of the user " +
                        mDBApi.accountInfo().displayName);
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            Log.i(getClass().getSimpleName(), "Local file name is " + params[0]);

            FileInputStream inputStream = null;
            try {
                File file = new File(params[0]);
                inputStream = new FileInputStream(file);
                DropboxAPI.Entry newEntry = mDBApi.putFile("test.wav", inputStream, file.length(),
                        null, null);

                Log.i(getClass().getSimpleName(), "Uploaded file path is " + newEntry.path);
                Log.i(getClass().getSimpleName(), "Sharing it");

                DropboxAPI.DropboxLink link = mDBApi.share(newEntry.path);
                Log.i(getClass().getSimpleName(), "URL is " + link.url);

            } catch (Exception e) {
                System.out.println("Something went wrong: " + e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {}
                }
            }
            return null;
        }
    }

    public void putFile(String localFilePath) {
        new UploadFileTask().execute(localFilePath);
    }
}
