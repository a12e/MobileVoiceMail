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

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("loginName", mLoginName);
        object.put("phoneNumber", mPhoneNumber);
        object.put("instantMessagingId", mInstantMessagingId);
        object.put("companyEmail", mCompanyEmail);
        return object;
    }
}
