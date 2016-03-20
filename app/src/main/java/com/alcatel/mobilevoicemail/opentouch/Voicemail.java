package com.alcatel.mobilevoicemail.opentouch;

import com.alcatel.mobilevoicemail.BaseVoicemail;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;

public class Voicemail extends BaseVoicemail {

    private int mDuration;
    private Boolean mUnread;
    private Boolean mHighPriority;
    private String mUrl;

    public static Voicemail fromJson(JSONObject object) throws JSONException, ParseException {
        Voicemail voicemail = new Voicemail();
        voicemail.mId = object.getString("voicemailId");
        voicemail.mFrom = Identifier.fromJson(object.getJSONObject("from").getJSONObject("id"));
        voicemail.mDuration = object.getInt("duration");
        voicemail.mDate = DateFormat.getInstance().parse(object.getString("date"));
        voicemail.mUnread = object.getBoolean("unread");
        voicemail.mHighPriority = object.getBoolean("highPriority");
        voicemail.mUrl = object.getString("url");

        return voicemail;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();

        return object;
    }

    public int getDuration() {
        return mDuration;
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
