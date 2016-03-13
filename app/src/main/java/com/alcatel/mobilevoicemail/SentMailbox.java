package com.alcatel.mobilevoicemail;

import java.util.ArrayList;

public class SentMailbox {

    private volatile static SentMailbox mInstance;
    private ArrayList<LocalVoicemail> mVoicemails;

    private SentMailbox() {
        mVoicemails = new ArrayList<>();
    }

    public static SentMailbox getInstance() {
        if(mInstance == null) {
            mInstance = new SentMailbox();
        }
        return mInstance;
    }

    public ArrayList<LocalVoicemail> getVoicemails() {
        return mVoicemails;
    }

    public void addVoicemail(LocalVoicemail voicemail) {
        mVoicemails.add(voicemail);
    }
}
