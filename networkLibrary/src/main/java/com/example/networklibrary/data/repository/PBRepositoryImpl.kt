package com.example.networklibrary.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.example.networklibrary.data.remote.PBApi
import com.example.networklibrary.domain.model.*
import com.example.networklibrary.domain.repository.PBRepository
import com.example.networklibrary.network.monitor.NetworkMonitor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException

class PBRepositoryImpl(private val api: PBApi, private val networkMonitor: NetworkMonitor, private val context: Context ) : PBRepository {

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

                    // Универсальная обработка ошибок
                    val errorDetails = try {
                        val type = object : TypeToken<Map<String, Any>>() {}.type
                        gson.fromJson<Map<String, Any>>(errorBody, type) ?: emptyMap()
                    } catch (ex: Exception) {
                        mapOf("raw" to errorBody)
                    }

                    NetworkResult.Error(
                        ApiError(
                            code = errorCode,
                            message = "HTTP Error: $errorCode",
                            details = errorDetails
                        )
                    )
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

//    override suspend fun logout(token:String, idToken: String): NetworkResult<Unit> =
//        safeApiCall { api.logout(token, idToken) }

    override suspend fun logout(token:String, idToken: String): NetworkResult<Unit> =
        safeApiCall {
            api.logout(token,idToken )
        }

    override suspend fun createProjectWithImage(request: RequestProjectImage): NetworkResult<Project> =
        safeApiCall {

            val fields = mapOf(
                "title" to request.title,
                "typeProject" to request.typeProject,
                "user_id" to request.user_id,
                "dateStart" to request.dateStart,
                "dateEnd" to request.dateEnd,
                "gender" to request.gender,
                "description_source" to request.description_source,
                "category" to request.category
            )

            val imagePart = request.imageUri?.let { uri ->
                val file = uri.toFile(context)
                val requestBody = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData(
                    "image",
                    request.imageFileName.ifEmpty { file.name },
                    requestBody
                )
            }

            api.createProjectWithImage(
                image = imagePart,
                fields = fields
            )
        }
}

private fun Uri.toFile(context: Context): File {
    val inputStream = context.contentResolver.openInputStream(this)
        ?: throw IOException("Cannot open file")

    val file = File(context.cacheDir, "image${System.currentTimeMillis()}.jpg")

    inputStream.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file
}