package com.alcatel.mobilevoicemail;

import com.alcatel.mobilevoicemail.opentouch.Identifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class LocalVoicemail extends BaseVoicemail {

    private String mUrl;

    public LocalVoicemail() {
        mId = UUID.randomUUID().toString();
        mFrom = Identifier.me();
        mDate = new Date();
    }

    public String getPath() {
        return App.getContext().getFilesDir().getAbsolutePath() + "/" + getId() + ".wav";
    }

    public static LocalVoicemail fromJson(JSONObject json) throws JSONException {
        LocalVoicemail voicemail = new LocalVoicemail();
        voicemail.mId = json.getString("id");
        voicemail.mFrom = Identifier.fromJson(json.getJSONObject("from"));
        voicemail.mDestination = Identifier.fromJson(json.getJSONObject("destination"));
        voicemail.mUrl = json.getString("url");
        voicemail.mDate = new Date(json.getLong("date"));
        return voicemail;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", mId);
        json.put("from", mFrom.toJson());
        json.put("destination", mDestination.toJson());
        json.put("url", mUrl);
        json.put("date", mDate.getTime());
        return json;
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
