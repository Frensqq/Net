package com.example.networklibrary.data.repository

import com.example.networklibrary.data.remote.PBApi
import com.example.networklibrary.domain.model.*
import com.example.networklibrary.domain.repository.PBRepository
import com.example.networklibrary.network.monitor.NetworkMonitor
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class PBRepositoryImpl(private val api: PBApi, private val networkMonitor: NetworkMonitor) : PBRepository {

    private val gson = Gson()

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {

        if (!networkMonitor.isConnected()) {
            return NetworkResult.NoInternet
        }

        return try {
            NetworkResult.Success(apiCall())
        } catch (e: Exception) {
            when (e) {
                is IOException -> NetworkResult.NoInternet
                is HttpException -> {
                    val errorCode = e.code()
                    val errorBody = e.response()?.errorBody()?.string() ?: "Unknown error"

                    // Специальная обработка для ошибки 400
                    if (errorCode == 400) {
                        try {
                            // Парсим ошибку в ваш существующий Error400
                            val error400 = gson.fromJson(errorBody, Error400::class.java)
                            NetworkResult.Error(
                                ApiError(
                                    code = errorCode,
                                    message = "Bad Request",
                                    error400 = error400
                                )
                            )
                        } catch (parseException: Exception) {
                            // Если не удалось распарсить как Error400
                            NetworkResult.Error(
                                ApiError(
                                    code = errorCode,
                                    message = errorBody
                                )
                            )
                        }
                    } else {
                        // Для других HTTP ошибок
                        NetworkResult.Error(
                            ApiError(
                                code = errorCode,
                                message = errorBody
                            )
                        )
                    }
                }
                is SocketTimeoutException -> NetworkResult.Error(
                    ApiError(code = 408, message = "Request timeout")
                )
                else -> NetworkResult.Error(
                    ApiError(code = -1, message = e.message ?: "Unknown error")
                )
            }
        }
    }

    override suspend fun registration(request: RequestRegister): NetworkResult<ResponseRegister> =
        safeApiCall { api.registration(request) }

    override suspend fun viewUser(idUser: String): NetworkResult<User> =
        safeApiCall { api.viewUser(idUser) }

    override suspend fun redactProfile(idUser: String, request: RequestUser): NetworkResult<User> =
        safeApiCall { api.redactProfile(idUser, request) }

    override suspend fun authorizationUser(request: RequestAuth): NetworkResult<ResponseAuth> =
        safeApiCall { api.authorizationUser(request) }

    override suspend fun returnIdToken(token: String): NetworkResult<UsersAuth> =
        safeApiCall { api.returnIdToken(token) }

    override suspend fun deleteUser(idToken: String): NetworkResult<Unit> =
        safeApiCall { api.deleteUser(idToken) }

    override suspend fun promoAndNews(): NetworkResult<ResponsesNews> =
        safeApiCall { api.promoAndNews() }

    override suspend fun listProduct(filter: String?): NetworkResult<ResponseProducts> =
        safeApiCall { api.listProduct(filter) }

    override suspend fun descriptionProduct(idProduct: String): NetworkResult<Product> =
        safeApiCall { api.descriptionProduct(idProduct) }

    override suspend fun listProject(filter: String?): NetworkResult<ResponsesProject> =
        safeApiCall { api.listProject(filter) }

    override suspend fun createProject(request: RequestProject): NetworkResult<Project> =
        safeApiCall { api.createProject(request) }

    override suspend fun listBucket(filter: String?): NetworkResult<ResponsesCart> =
        safeApiCall { api.listCart(filter) }
    override suspend fun createBucket(request: RequestCart): NetworkResult<ResponseCart> =
        safeApiCall { api.createBucket(request) }

    override suspend fun deleteBucket(id: String): NetworkResult<Unit> =
        safeApiCall { api.deleteBucket(id) }

    override suspend fun redactBucket(idBucket: String, request: RequestCart): NetworkResult<ResponseCart> =
        safeApiCall { api.redactBucket(idBucket, request) }

    override suspend fun listOrders(filter: String?): NetworkResult<ResponsesOrders> =
        safeApiCall { api.listOrders(filter) }
    override suspend fun createOrder(request: RequestOrder): NetworkResult<ResponseOrder> =
        safeApiCall { api.createOrder(request) }

    override suspend fun logout(token:String, idToken: String): NetworkResult<Unit> =
        safeApiCall { api.logout(token, idToken) }
}