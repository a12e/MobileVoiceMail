package com.alcatel.mobilevoicemail.opentouch;

import org.json.JSONException;
import org.json.JSONObject;

public class PartyInfo {
    private Identifier mIdentifier;
    private String mFirstName;
    private String mLastName;

    public static PartyInfo fromJson(JSONObject object) throws JSONException {
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.mIdentifier = object.has("id") ? Identifier.fromJson(object.getJSONObject("id")) : null;
        partyInfo.mFirstName = object.has("firstName") ? object.getString("firstName") : null;
        partyInfo.mLastName = object.has("lastName") ? object.getString("lastName") : null;
        return partyInfo;
    }

    public Identifier getIdentifier() {
        return mIdentifier;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }
}
