package com.pertamina.absensiapplication.api

import com.pertamina.absensiapplication.model.Permit
import com.pertamina.absensiapplication.model.User
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PermitApi {
    @GET("auth/{email}/{password}")
    fun createAccountAsync(@Path("email") email: String, @Path("password") password: String): Deferred<String>

    @GET("organicLeave")
    fun getOrganicLeave(): Deferred<Int>

    @GET("tkjpLeave")
    fun getTkjpLeave(): Deferred<Int>

    @POST("auth/{uid}/{email}/{password}")
    fun updateAccountAsync(@Path("uid") uid: String, @Path("email") email: String, @Path("password") password: String): Deferred<String>

    @POST("user/{id}")
    fun createUserDataAsync(@Path("id") userId: String, @Body user: User): Deferred<User>

    @POST("permit/{id}")
    fun createPermitManualAsync(@Path("id") userId: String, @Body permit: Permit): Deferred<Permit>
}