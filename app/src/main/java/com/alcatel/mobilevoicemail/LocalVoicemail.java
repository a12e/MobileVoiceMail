package com.alcatel.mobilevoicemail;

import java.util.UUID;

public class LocalVoicemail extends BaseVoicemail {

    public LocalVoicemail() {
        mId = UUID.randomUUID().toString();
    }

    public String getPath() {
        return App.getContext().getFilesDir().getAbsolutePath() + "/" + getId() + ".wav";
    }

}
