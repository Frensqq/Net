package com.example.networklibrary.data.remote

import com.example.networklibrary.domain.model.Product
import com.example.networklibrary.domain.model.Project
import com.example.networklibrary.domain.model.RequestAuth
import com.example.networklibrary.domain.model.RequestCart
import com.example.networklibrary.domain.model.RequestOrder
import com.example.networklibrary.domain.model.RequestProject
import com.example.networklibrary.domain.model.RequestRegister
import com.example.networklibrary.domain.model.RequestUser
import com.example.networklibrary.domain.model.ResponseAuth
import com.example.networklibrary.domain.model.ResponseCart
import com.example.networklibrary.domain.model.ResponseOrder
import com.example.networklibrary.domain.model.ResponseProducts
import com.example.networklibrary.domain.model.ResponseRegister
import com.example.networklibrary.domain.model.ResponsesCart
import com.example.networklibrary.domain.model.ResponsesNews
import com.example.networklibrary.domain.model.ResponsesOrders
import com.example.networklibrary.domain.model.ResponsesProject
import com.example.networklibrary.domain.model.User
import com.example.networklibrary.domain.model.UserAuth
import com.example.networklibrary.domain.model.UsersAuth
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PBApi {

    //user Действия с пользователем
    @POST("collections/users/records")
    suspend fun registration(@Body request: RequestRegister): ResponseRegister

    @GET("collections/users/records/{id_user}")
    suspend fun viewUser(@Path("id_user") id_user: String): User

    @PATCH("collections/users/records/{id_user}")
    suspend fun redactProfile(@Path("id_user") id_user: String, @Body request: RequestUser): User

    @POST("collections/users/auth-with-password")
    suspend fun authorizationUser(@Body request: RequestAuth): ResponseAuth

    @GET("collections/_authOrigins/records")
    suspend fun returnIdToken(@Header("Authorization") token: String): UsersAuth

    @DELETE("collections/users/records/{id_token}")
    suspend fun deleteUser(@Path("id_token") id_token: String): Unit

    //shop Действия с магазином
    @GET("collections/news/records")
    suspend fun promoAndNews(): ResponsesNews

    @GET("collections/products/records")
    suspend fun listProduct(@Query("filter") filter: String? = null): ResponseProducts

    @GET("collections/products/records/{id_product}")
    suspend fun descriptionProduct(@Path("id_product") id_product: String): Product

    //project Действия с проектами
    @GET("collections/project/records")
    suspend fun listProject(@Query("filter") filter: String? = null): ResponsesProject

    @POST("collections/project/records")
    suspend fun createProject(@Body request: RequestProject): Project

    //backet Действия с корзиной

    @GET("collections/cart/records")
    suspend fun listCart(@Query("filter") filter: String? = null): ResponsesCart
    @POST("collections/cart/records")
    suspend fun createBucket(@Body request: RequestCart): ResponseCart

    @DELETE("collections/cart/records/{id}")
    suspend fun deleteBucket(@Path("id") id: String): Unit

    @PATCH("collections/cart/records/{id_bucket}")
    suspend fun redactBucket(@Path("id_bucket") id_bucket: String, @Body request: RequestCart): ResponseCart

    //order Действия с заказом

    @GET("collections/cart/records")
    suspend fun listOrders(@Query("filter") filter: String? = null): ResponsesOrders
    @POST("collections/orders/records")
    suspend fun createOrder(@Body request: RequestOrder): ResponseOrder

    //выход
    @DELETE("collections/_authOrigins/records/{id_token}")
    suspend fun logout(@Header("Authorization") token: String, @Path("id_token") id_token: String): Unit

}