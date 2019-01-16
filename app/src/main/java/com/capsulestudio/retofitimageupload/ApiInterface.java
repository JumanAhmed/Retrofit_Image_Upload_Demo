package com.capsulestudio.retofitimageupload;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Juman on 10/13/2017.
 */

public interface ApiInterface {

    @GET("getBikeOrCarJson.php")
    Call<List<DataModel>> getVehiclesWithGetMethod(@Query("type") String type);

    @FormUrlEncoded
    @POST("getBikeOrCarJsonUsingPost.php")
    Call<List<DataModel>> getVehiclesWithPostMethod(@Field("type") String type);

    @Multipart
    @POST("addNewCars.php")
    Call<DataModel> uploadCarData(
            @Part("brandName") RequestBody brandName,
            @Part("model") RequestBody model,
            @Part MultipartBody.Part file
            );
}
