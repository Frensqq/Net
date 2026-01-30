package com.example.networklibrary

import com.example.networklibrary.data.remote.PBApi
import com.example.networklibrary.domain.model.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit




class PBRepositoryUnitTest {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://127.0.0.1:8090/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build())
        .build()

    private val api = retrofit.create(PBApi::class.java)
    var email = "test${System.currentTimeMillis()}@example.com"


    private fun regUser(email: String): String = runBlocking {
        api.registration(RequestRegister(
            email = email,
            password = "password123",
            passwordConfirm = "password123",
            firstname = "Test",
            secondname = "User",
            lastname = "Test",
            datebirthday = "2007-01-01",
            gender = "male"
        )).id
    }
    private val userId = regUser(email)

    private fun auth(): String = runBlocking {
        api.authorizationUser(RequestAuth(email, "password123")).token
    }

    private val token = auth()

    private fun searchLogoutId(): String = runBlocking {
        api.returnIdToken(token).items.first { it.recordRef == userId }.id
    }

    private fun getProductId(): String = runBlocking {
        api.listProduct().items.first().id
    }

    private val productId = getProductId()

    private fun addCart(): String = runBlocking {
        api.createBucket(RequestCart(userId, productId, 1)).id
    }

    private val cartId = addCart()



    @Test
    fun CreateProfile() {
        runBlocking {
            regUser("test${System.currentTimeMillis()}@example.com")
        }
    }

    @Test
    fun Authorization() {
        auth()
    }
    @Test
    fun GetPromotions() {
        runBlocking {
            api.promoAndNews()
        }
    }

    @Test
    fun GetCatalog() {
        runBlocking {
            api.listProduct()
        }
    }

    @Test
    fun Search() {
        runBlocking {
            api.listProduct("title~'nike'")
        }
    }

    @Test
    fun GetProductDescription() {
        runBlocking {
            val products = api.listProduct()
            if (products.items.isNotEmpty()) {
                api.descriptionProduct(products.items[0].id)
            }
        }
    }

    @Test
    fun AddToCart() {
        addCart()
    }

    @Test
    fun UpdateCart() {
        runBlocking {
            val products = api.listProduct()
            api.redactBucket(cartId, RequestCart(userId, products.items[0].id, 2))
        }
    }

    @Test
    fun CreateOrder() {
        runBlocking {
            api.createOrder(RequestOrder("", "", 1))
        }
    }

    @Test
    fun GetProjects() {
        runBlocking {
            api.listProject()
        }
    }

    @Test
    fun CreateProject() {
        runBlocking {
            api.createProject(RequestProject(
                title = "Тестовый проект ${System.currentTimeMillis()}",
                typeProject = "web",
                user_id = "",
                dateStart = "2024-01-01",
                dateEnd = "2024-12-31",
                gender = "male",
                description_source = "Тестовое описание",
                category = "development"
            ))
        }
    }

    @Test
    fun GetUserProfile() {
        runBlocking {
            api.viewUser(userId)
        }
    }

//    @Test
//    fun Logout() {
//        runBlocking {
//            api.logout(token, searchLogoutId())
//        }
//    }
}