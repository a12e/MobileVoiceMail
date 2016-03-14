package com.alcatel.mobilevoicemail;

import android.content.Context;
import android.util.Log;

import com.alcatel.mobilevoicemail.opentouch.OpenTouchClient;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SentMailbox {

    private volatile static SentMailbox mInstance;
    private ArrayList<LocalVoicemail> mVoicemails;

    private SentMailbox() {
        mVoicemails = new ArrayList<>();
        load();
    }

    public static SentMailbox getInstance() {
        if(mInstance == null) {
            mInstance = new SentMailbox();
        }
        return mInstance;
    }

    private void save() {
        try {
            JSONArray jsonArray = new JSONArray();
            for(LocalVoicemail voicemail: mVoicemails) {
                jsonArray.put(voicemail.toJson());
            }

            final String fileName = OpenTouchClient.getInstance().getLoginName() + ".json";
            Log.i(SentMailbox.class.getSimpleName(), "Saving the SentMailbox to " + fileName);
            FileOutputStream outputStream = App.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(jsonArray.toString().getBytes());
            outputStream.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        mVoicemails.clear();

        try {
            final String fileName = OpenTouchClient.getInstance().getLoginName() + ".json";
            Log.i(SentMailbox.class.getSimpleName(), "Loading the SentMailbox from " + fileName);
            FileInputStream inputStream = App.getContext().openFileInput(fileName);
            java.util.Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String jsonString = s.hasNext() ? s.next() : "";

            JSONArray jsonArray = new JSONArray(jsonString);
            for(int i = 0; i < jsonArray.length(); i++) {
                mVoicemails.add(LocalVoicemail.fromJson(jsonArray.getJSONObject(i)));
            }

            Log.i(SentMailbox.class.getSimpleName(), "Loaded " + mVoicemails.size() + " sent messages");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LocalVoicemail> getVoicemails() {
        return mVoicemails;
    }

    public void addVoicemail(LocalVoicemail voicemail) {
        mVoicemails.add(voicemail);
        save();
    }
}
