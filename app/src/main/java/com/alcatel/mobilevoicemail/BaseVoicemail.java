package com.alcatel.mobilevoicemail;

import com.alcatel.mobilevoicemail.opentouch.Identifier;

import java.util.Date;

// Classe de base pour représenter les messages vocaux
// Dérivée en deux variantes
// -> LocalVoicemail        (pour les messages envoyés, stockés uniquement en local)
// -> Opentouch.Voicemail   (pour les messages reçus)
public abstract class BaseVoicemail {

    protected String mId;
    protected Identifier mFrom;
    protected Identifier mDestination;
    protected Date mDate;

    public String getId() {
        return mId;
    }

    public Identifier getFrom() {
        return mFrom;
    }

    public Identifier getDestination() {
        return mDestination;
    }

    public Date getDate() {
        return mDate;
    }

}
