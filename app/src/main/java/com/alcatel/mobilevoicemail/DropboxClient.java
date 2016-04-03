package com.alcatel.mobilevoicemail;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static java.net.URLDecoder.decode;

public class DropboxClient {
    private static DropboxClient instance = null;

    final static private String APP_KEY = "ydma9rv0xm4wf4d";
    final static private String APP_SECRET = "ei7oouuh13yfp4l";
    final static private String APP_OATOKEN = "rnLcn28O8aAAAAAAAAAADBleuAZTS0BR9D0i0jDCILtqaUJHd489W0mJjHJwsvmn";
    private DropboxAPI<AndroidAuthSession> mDBApi = null;

    private DropboxClient() {
        Log.d(getClass().getSimpleName(), "Creating DropboxClient");
        try {
            AppKeyPair keyPair = new AppKeyPair(APP_KEY, APP_SECRET);
            mDBApi = new DropboxAPI<>(new AndroidAuthSession(keyPair, APP_OATOKEN));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        Log.d(getClass().getSimpleName(), "Created DropboxClient");
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

    private class UploadFileTask extends AsyncTask<LocalVoicemail, Void, Void> {
        @Override
        protected Void doInBackground(LocalVoicemail... params) {
            if(params.length != 1) {
                throw new IllegalArgumentException("Only one parameter please");
            }
            LocalVoicemail voicemail = params[0];

            Log.d(getClass().getSimpleName(), "Sending message to Dropbox");

            try {
                Log.i(getClass().getSimpleName(), "Using dropbox of the user " +
                        mDBApi.accountInfo().displayName);
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            Log.i(getClass().getSimpleName(), "Local file name is " + voicemail.getPath());

            FileInputStream inputStream = null;
            try {
                File file = new File(voicemail.getPath());
                inputStream = new FileInputStream(file);
                DropboxAPI.Entry newEntry = mDBApi.putFile(file.getName(), inputStream,
                        file.length(), null, null);

                Log.i(getClass().getSimpleName(), "Uploaded file path is " + newEntry.path);
                Log.i(getClass().getSimpleName(), "Sharing it");

                DropboxAPI.DropboxLink link = mDBApi.share(newEntry.path);
                Log.i(getClass().getSimpleName(), "Dropbox shorten URL is " + link.url);

                // We now need to get the real URL of the message, beaucase Dropbox provides a shorten URL
                HttpsURLConnection connection = (HttpsURLConnection)new URL(link.url).openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                // Get the target URL of the shorten link
                String realURL = connection.getHeaderField("Location").replace("?dl=0", "?dl=1");
                realURL = decode(realURL, "UTF-8");
                Log.i(getClass().getSimpleName(), "Dropbox real URL is " + realURL);
                voicemail.setUrl(realURL);

                // Add the message to the SentMailbox
                SentMailbox.getInstance().addVoicemail(voicemail);

                // Notify the app that the message has been successfully sent to Dropbox
                Intent messageUploadedIntent = new Intent("MESSAGE_UPLOADED");
                messageUploadedIntent.putExtra("destination", voicemail.getDestination().getPhoneNumber());
                messageUploadedIntent.putExtra("url", voicemail.getUrl());
                App.getContext().sendBroadcast(messageUploadedIntent);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Something went wrong: " + e);
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

    public void uploadVoicemail(LocalVoicemail voicemail) {
        new UploadFileTask().execute(voicemail);
    }
}
