package com.alcatel.mobilevoicemail.opentouch;

import org.json.JSONException;
import org.json.JSONObject;

public class Identifier {
    private String mLoginName;
    private String mPhoneNumber;
    private String mInstantMessagingId;
    private String mCompanyEmail;

    public Identifier(String loginName) {
        this.mLoginName = loginName;
    }

    public static Identifier fromJson(JSONObject object) throws JSONException {
        Identifier identifier = new Identifier(object.getString("loginName"));
        identifier.mPhoneNumber = object.getString("phoneNumber");
        identifier.mInstantMessagingId = object.getString("instantMessagingId");
        identifier.mCompanyEmail = object.getString("companyEmail");
        return identifier;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("loginName", mLoginName);
        object.put("phoneNumber", mPhoneNumber);
        object.put("instantMessagingId", mInstantMessagingId);
        object.put("companyEmail", mCompanyEmail);
        return object;
    }
}
