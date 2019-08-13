package com.example.dogsapi

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface APIServer {
    @GET("breeds/list/all")
    suspend fun getBreedsList(
    ): Response<Any>

    @GET
    suspend fun getImageURL( @Url url:String ): Response<Any>
}

