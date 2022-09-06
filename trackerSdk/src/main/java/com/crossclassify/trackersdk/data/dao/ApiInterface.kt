package com.crossclassify.trackersdk.data.dao

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface ApiInterface {
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=utf-8")
    @GET
    fun sendData(@Url url: String): Call<ResponseBody>
}