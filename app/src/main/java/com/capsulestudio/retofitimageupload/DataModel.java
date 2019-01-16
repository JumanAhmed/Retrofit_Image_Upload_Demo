package com.capsulestudio.retofitimageupload;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Juman on 10/18/2017.
 */

public class DataModel {
    @SerializedName("response")
    private String Response;

    public String getResponse() {
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }
}
