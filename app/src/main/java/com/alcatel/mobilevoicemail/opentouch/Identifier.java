package com.alcatel.mobilevoicemail.opentouch;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Identifier {
    private String mLoginName;
    private String mPhoneNumber;
    private String mInstantMessagingId;
    private String mCompanyEmail;

    // useful only in ThreadsActivity
    public Date lastVoicemailDate;

    public Identifier(String phoneNumber) {
        mLoginName = "";
        mPhoneNumber = phoneNumber;
        mInstantMessagingId = "";
        mCompanyEmail = "";
    }

    // Retourne un Identifier de la personne connectée
    public static Identifier me() {
        return new Identifier(OpenTouchClient.getInstance().getLoginName());
    }

    public static Identifier fromJson(JSONObject object) throws JSONException {
        Identifier identifier = new Identifier(object.getString("phoneNumber"));
        identifier.mLoginName = object.getString("loginName");
        identifier.mInstantMessagingId = object.getString("instantMessagingId");
        identifier.mCompanyEmail = object.getString("companyEmail");
        return identifier;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("loginName", mLoginName.equals("") ? null : mLoginName);
        object.put("phoneNumber", mPhoneNumber.equals("") ? null : mPhoneNumber);
        object.put("instantMessagingId", mInstantMessagingId.equals("") ? null : mInstantMessagingId);
        object.put("companyEmail", mCompanyEmail.equals("") ? null : mCompanyEmail);
        return object;
    }

    public String getLoginName() {
        return mLoginName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getInstantMessagingId() {
        return mInstantMessagingId;
    }

    public String getCompanyEmail() {
        return mCompanyEmail;
    }

    public String getDisplayName() {
        String displayName = mLoginName;
        if(mPhoneNumber != null && !mPhoneNumber.equals("")) {
            displayName = displayName.concat(" (" + mPhoneNumber + ")");
        }
        return displayName;
    }
}
