package com.alcatel.mobilevoicemail.opentouch;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Identifier implements Serializable {
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
        identifier.mLoginName = object.has("loginName") ? object.getString("loginName") : null;
        identifier.mInstantMessagingId = object.has("instantMessagingId") ? object.getString("instantMessagingId") : null;
        identifier.mCompanyEmail = object.has("companyEmail") ? object.getString("companyEmail") : null;
        return identifier;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("loginName", mLoginName != null ? mLoginName : JSONObject.NULL);
        object.put("phoneNumber", mPhoneNumber != null ? mPhoneNumber : JSONObject.NULL);
        object.put("instantMessagingId", mInstantMessagingId != null ? mInstantMessagingId : JSONObject.NULL);
        object.put("companyEmail", mCompanyEmail != null ? mCompanyEmail : JSONObject.NULL);
        return object;
    }

    private static final long serialVersionUID = 258558687L;

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
        String displayName = "";
        if(mLoginName != null && !mLoginName.equals("")) {
            displayName = displayName.concat(mLoginName);
        }
        if (mPhoneNumber != null && !mPhoneNumber.equals("")) {
            if(displayName.equals("")) {
                displayName = mPhoneNumber;
            }
            else {
                displayName = displayName.concat(" (" + mPhoneNumber + ")");
            }
        }
        return displayName;
    }
}
