package com.alcatel.mobilevoicemail;

import com.alcatel.mobilevoicemail.opentouch.Identifier;

// Classe de base pour représenter les messages vocaux
// Dérivée en deux variantes
// -> LocalVoicemail
// -> Opentouch.Voicemail
public abstract class BaseVoicemail {

    protected String mId;
    protected Identifier mFrom;
    protected Identifier mDestination;

    public String getId() {
        return mId;
    }

    public Identifier getFrom() {
        return mFrom;
    }

    public Identifier getDestination() {
        return mDestination;
    }
}
