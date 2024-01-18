package com.freelancing.zhenisapp;

// ObjectApi.java
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Multipart;
import okhttp3.MultipartBody;

public interface ObjectApi {
    @Multipart
    @POST("measure")
    Call<MeasurementResponse> measureObject(
            @Part MultipartBody.Part image
    );
}
