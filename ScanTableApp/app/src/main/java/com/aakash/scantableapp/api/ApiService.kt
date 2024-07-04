package com.aakash.scantableapp.api

import com.aakash.scantableapp.model.ImageModel
import com.aakash.scantableapp.model.TableModel
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiService {

    @Multipart
    @POST("/s3/upload") // Adjust the URL based on your server endpoint
    fun uploadFile(@Part file: MultipartBody.Part): retrofit2.Call<TableModel>
}