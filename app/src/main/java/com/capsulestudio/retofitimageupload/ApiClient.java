package com.capsulestudio.retofitimageupload;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Juman on 10/10/2017.
 */

public class ApiClient {
    public static final String Base_Url ="http://juman.com/file/";
    public static Retrofit retrofit = null;

    public static Retrofit  getApiClient(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(Base_Url).
                    addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

    public static ApiInterface getApiInterface() {
        return getApiClient().create(ApiInterface.class);
    }

}
