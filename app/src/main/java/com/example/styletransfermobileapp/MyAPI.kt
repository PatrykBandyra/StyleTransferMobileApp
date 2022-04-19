package com.example.styletransfermobileapp

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyAPI {
    @Multipart
    @POST("nst/")
    fun uploadImage(
        @Part file: MultipartBody.Part,
//        @Part("desc") desc: RequestBody
    ): Call<ResponseBody>

    companion object {
        operator fun invoke(): MyAPI {
            return Retrofit.Builder()
                .baseUrl("http://20.113.133.133:80/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyAPI::class.java)
        }
    }
}