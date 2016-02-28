package com.alcatel.mobilevoicemail.opentouch;

import com.alcatel.mobilevoicemail.BaseVoicemail;

import org.json.JSONException;
import org.json.JSONObject;

public class Voicemail extends BaseVoicemail {

    private Identifier mFrom;
    private int mDuration;
    private String mDate;
    private Boolean mUnread;
    private Boolean mHighPriority;
    private String mUrl;

    public static Voicemail fromJson(JSONObject object) throws JSONException {
        Voicemail voicemail = new Voicemail();
        voicemail.mId = object.getString("voicemailId");
        voicemail.mFrom = Identifier.fromJson(object.getJSONObject("from").getJSONObject("id"));
        voicemail.mDuration = object.getInt("duration");
        voicemail.mDate = object.getString("date");
        voicemail.mUnread = object.getBoolean("unread");
        voicemail.mHighPriority = object.getBoolean("highPriority");
        voicemail.mUrl = object.getString("url");

        return voicemail;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();

        return object;
    }

    public Identifier getFrom() {
        return mFrom;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getDate() {
        return mDate;
    }

    public Boolean getUnread() {
        return mUnread;
    }

    public Boolean getHighPriority() {
        return mHighPriority;
    }

    public String getUrl() {
        return mUrl;
    }
}
