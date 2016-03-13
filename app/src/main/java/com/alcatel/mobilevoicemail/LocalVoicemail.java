package com.alcatel.mobilevoicemail;

import com.alcatel.mobilevoicemail.opentouch.Identifier;

import java.util.UUID;

public class LocalVoicemail extends BaseVoicemail {

    private String mUrl;

    public LocalVoicemail() {
        mId = UUID.randomUUID().toString();
        mFrom = Identifier.me();
    }

    public String getPath() {
        return App.getContext().getFilesDir().getAbsolutePath() + "/" + getId() + ".wav";
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public void setDestination(Identifier to) {
        this.mDestination = to;
    }

}
