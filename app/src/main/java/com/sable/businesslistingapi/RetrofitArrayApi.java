package com.sable.businesslistingapi;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by Jaink on 14-09-2017.
 */



public interface RetrofitArrayApi {

    @GET("wp-json/geodir/v2/business")
    Call<List<BusinessListings>> getPostInfo(
            @QueryMap Map<String, String> options
    );

    @GET("wp-json/geodir/v2/business/?post_type=gd_business")
    Call<List<BusinessListings>> getSearch(
            @Query("search") String query);

    @GET("wp-json/geodir/v2/business/?post_type=gd_business")
    Call<List<BusinessListings>> getRadius(
            @QueryMap Map<String, String> options
    );

    @GET("wp-json/wc/v3/products?consumer_key=ck_c3addab1f230fa55025a2f78969d18f518ebbc5e&consumer_secret=cs_aaf9c39669e92ebd745a0e91a9a5810e9222cc92")
    Call<List<WooProducts>> getPostWooInfo();

    @GET("wp-json/geodir/v2/business/categories")
    Call<List<ListingsCategories>> getCategory();

/// to make call to dynamic URL
 // @GET
  //Call<List<BusinessListings>> getPostInfo(@Url String url);
//
    @POST("wp-json/geodir/v2/business/")
    Call<List<BusinessListings>> postData(
            @Query("title") String title,
            @Query("status") String status,
            @Query("post_category") Integer category,
            @Query("content")String content,
            @Query("bldgno") String bldgno,
            @Query("street") String street,
            @Query("city") String city,
            @Query("region") String state,
            @Query("country") String country,
            @Query("zip") String zip,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Query("phone") String phone,
            @Query("email") String email,
            @Query("website") String website,
            @Query("twitter") String twitter,
            @Query("facebook") String facebook);

    @Multipart
    @POST("wp-json/geodir/v2/business/")
    Call<List<BusinessListings>> postReview(
            @Query("link") String link,
            @Query("title") String title,
            @Query("status") String status,
            @Query("rating") Float rating,
            @Query("post_type")String content,
            @Query("city") String city,
            @Query("region") String state,
            @Query("country") String country,
            @Query("zip") String zip,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude,
            @Part MultipartBody.Part image00,
            @Part MultipartBody.Part image01,
            @Part MultipartBody.Part image02,
            @Part MultipartBody.Part image03,
            @Part MultipartBody.Part image04,
            @Part MultipartBody.Part image05);
 }

