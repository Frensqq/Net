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
    val email = "test${System.currentTimeMillis()}@example.com"


    @Test
    fun CreateProfile() {
        runBlocking {

            user = api.registration(RequestRegister(
                email = email,
                password = "password123",
                passwordConfirm = "password123",
                firstname = "Test",
                secondname = "User",
                lastname = "Test",
                datebirthday = "2000-01-01",
                gender = "male"
            ))
        }
    }

    @Test
    fun Authorization() {
        runBlocking {
            api.authorizationUser(RequestAuth(email, "password123"))
        }
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
            val response = api.listProduct()
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
        runBlocking {
            val products = api.listProduct()
            if (products.items.isNotEmpty()) {
                api.createBucket(RequestCart("", products.items[0].id, 1))
            }
        }
    }

    @Test
    fun UpdateCart() {
        runBlocking {
            api.redactBucket("", RequestCart("", "", 2))
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
            api.viewUser("")
        }
    }

    @Test
    fun Logout() {
        runBlocking {
            api.logout("Bearer token123", "auth_id_123")
        }
    }
}