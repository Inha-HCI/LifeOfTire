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

    @GET("API/insert_ex_data.asp")
    fun insert_ex_data(
        @Query("sid") sid:String,
        @Query("depth1") depth1:String,
        @Query("depth2") depth2:String,
        @Query("depth3") depth3:String,
        @Query("depth4") depth4:String,
        @Query("depth5") depth5:String,
        @Query("depth6") depth6:String,
        @Query("depth7") depth7:String,
        @Query("depth8") depth8:String,
        @Query("depth9") depth9:String,
        @Query("depth10") depth10:String,
        @Query("depth11") depth11:String,
        @Query("depth12") depth12:String,
    ): Call<Result_insert_ex_data>
}