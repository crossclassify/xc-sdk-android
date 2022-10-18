package com.crossclassify.trackersdk.data.dao

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface ApiInterface {
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=utf-8",
        "Connection: keep-alive"
        ,"x-api-key: Wz5C96h5dg37j4tlmVt3b6UD4O1GDLv34fHmfp6l")
    @GET
    fun sendData(@Url url: String): Call<ResponseBody>
}