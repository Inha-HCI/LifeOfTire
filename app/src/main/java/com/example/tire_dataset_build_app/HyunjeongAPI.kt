package com.example.tire_dataset_build_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDateTime
import java.util.*


interface HyunjeongAPI{
    @GET("API/new_experiment.asp")

    fun getResult(
        @Query("ex_date") date: String,
        @Query("experimenter") name: String,
        @Query("ex_place") place: String,
        @Query("tire_model") model: String,
        @Query("ex_round") round: Int? = null
    ): Call<ResultFromAPI>
}