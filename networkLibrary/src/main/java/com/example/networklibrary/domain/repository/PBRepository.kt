package com.example.networklibrary.domain.repository

import com.example.networklibrary.domain.model.*

/**
 * Абстракция для сетевого слоя - позволяет заменить реализацию без изменения бизнес-логики
 */
interface PBRepository {
    // User actions
    suspend fun registration(request: RequestRegister): NetworkResult<ResponseRegister>
    suspend fun viewUser(idUser: String): NetworkResult<User>
    suspend fun redactProfile(idUser: String, request: RequestUser): NetworkResult<User>
    suspend fun authorizationUser(request: RequestAuth): NetworkResult<ResponseAuth>
    suspend fun returnIdToken(token: String): NetworkResult<UsersAuth>
    suspend fun deleteUser(idToken: String): NetworkResult<Unit>

    // Shop actions
    suspend fun promoAndNews(): NetworkResult<ResponsesNews>
    suspend fun listProduct(filter: String? = null): NetworkResult<ResponseProducts>
    suspend fun descriptionProduct(idProduct: String): NetworkResult<Product>

    // Project actions
    suspend fun listProject(): NetworkResult<ResponsesProject>
    suspend fun createProject(request: RequestProject): NetworkResult<Project>

    // Cart actions
    suspend fun createBucket(request: RequestCart): NetworkResult<ResponseCart>
    suspend fun redactBucket(idBucket: String, request: RequestCart): NetworkResult<ResponseCart>

    // Order actions
    suspend fun createOrder(request: RequestOrder): NetworkResult<ResponseOrder>

    // Выход
    suspend fun logout(token: String, itToken: String): NetworkResult<Unit>

}