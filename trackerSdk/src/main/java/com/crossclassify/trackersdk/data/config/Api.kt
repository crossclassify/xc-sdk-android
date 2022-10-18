package com.crossclassify.trackersdk.data.config

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.crossclassify.trackersdk.data.dao.ApiInterface
import com.crossclassify.trackersdk.utils.base.TrackerSdkApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object Api {
    private var baseUrl = "https://api.crossclassify.com/matomo/"

    private var client: ApiInterface? = null
    fun client(context: Context): ApiInterface {
        return if (client == null) {
            val interceptor by lazy {
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder
                        .addHeader("User-Agent", TrackerSdkApplication.userAgent)
                    chain.proceed(builder.build())
                }
            }
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .build()
            ).addInterceptor(interceptor).addInterceptor(loggingInterceptor)
                .build()

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
            client = retrofit.create(ApiInterface::class.java)
            client!!
        } else {
            client!!
        }
    }
}