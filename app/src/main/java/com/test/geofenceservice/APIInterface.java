package com.test.geofenceservice;



import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("api.php/")
    Call<ResponseBody> getAccessToken(@Query("action") String action,
                                      @Query("unique_api_key") String uniqueApiKey,
                                      @Query("device_id") String deviceId
    );

    @GET("api.php/")
    Call<ResponseBody> getChapsNearby(@Query("action") String action,
                                      @Query("latitude") double latitude,
                                      @Query("longitude") double longitude,
                                      @Query("radius") String radius,
                                      @Query("unique_api_key") String uniqueApiKey,
                                      @Query("device_id") String deviceId,
                                      @Header("Bearer-Token") String authHeader
    );

    @GET("api.php/")
    Call<ResponseBody> getChapDetail(@Query("action") String action,
                                     @Query("id") Integer id,
                                     @Query("device_id") String deviceId,
                                     @Header("Bearer-Token") String authHeader
    );

    @GET("api.php/")
    Call<ResponseBody> getCollectedItems(@Query("action") String action,
                                         @Query("start") int start,
                                         @Query("limit") int limit,
                                         @Query("device_id") String deviceId,
                                         @Header("Bearer-Token") String authHeader
    );

     // annotation used in POST type requests
    @GET("api.php/")   // API's endpoints
     Call<ResponseBody> postViewed(@Query("action") String action,
                                   @Query("id") String idList,
                                   @Query("device_id") String deviceId,
                                   @Header("Bearer-Token") String authHeader);

    @GET("api.php/")     // API's endpoints
     Call<ResponseBody> postOpened(@Query("action") String action,
                                   @Query("id") String idList,
                                   @Query("device_id") String deviceId,
                                   @Header("Bearer-Token") String authHeader);

    @GET("api.php/")    // API's endpoints
     Call<ResponseBody> postInteracted(@Query("action") String action,
                                       @Query("id") String idList,
                                       @Query("device_id") String deviceId,
                                       @Header("Bearer-Token") String authHeader);
    @GET("api.php/")    // API's endpoints
     Call<ResponseBody> deleteChap(@Query("action") String action,
                                   @Query("id") Integer id,
                                   @Query("device_id") String deviceId,
                                   @Header("Bearer-Token") String authHeader);

    @POST("ping")
     Call<ResponseBody> notify(@Body NotificationModel model);

    @POST("pinggeo")
    Call<ResponseBody> notify1(@Body NotificationModel model);

    @POST("notifications")
    Call<ResponseBody> notification(@Body NotifyModel model);

}