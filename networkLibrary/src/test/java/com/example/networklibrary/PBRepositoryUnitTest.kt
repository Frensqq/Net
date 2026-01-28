package com.example.networklibrary



import android.content.Context
import com.example.networklibrary.data.remote.PBApi
import com.example.networklibrary.data.remote.PBApiServis
import com.example.networklibrary.data.repository.PBRepositoryImpl
import com.example.networklibrary.domain.model.*
import com.example.networklibrary.network.monitor.NetworkMonitor
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Request

class PBRealTests {

    // Используем 127.0.0.1 вместо 10.0.2.2
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://127.0.0.1:8090/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build())
        .build()

    private val api = retrofit.create(PBApi::class.java)

    @Test
    fun test1_Authorization() {
        runBlocking {
            api.authorizationUser(RequestAuth("test@example.com", "password123"))
        }
    }

    @Test
    fun test2_CreateProfile() {
        runBlocking {
            val email = "test${System.currentTimeMillis()}@example.com"
            api.registration(RequestRegister(
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
    fun test3_GetPromotions() {
        runBlocking {
            api.promoAndNews()
        }
    }

    @Test
    fun test4_GetCatalog() {
        runBlocking {
            val response = api.listProduct()
            println("Получено ${response.items.size} продуктов")
        }
    }

    @Test
    fun test5_Search() {
        runBlocking {
            api.listProduct("title~'nike'")
        }
    }

    @Test
    fun test6_GetProductDescription() {
        runBlocking {
            val products = api.listProduct()
            if (products.items.isNotEmpty()) {
                api.descriptionProduct(products.items[0].id)
            }
        }
    }

    @Test
    fun test7_AddToCart() {
        runBlocking {
            // Нужен существующий пользователь и продукт
            val products = api.listProduct()
            if (products.items.isNotEmpty()) {
                api.createBucket(RequestCart("user_id", products.items[0].id, 1))
            }
        }
    }

    @Test
    fun test8_UpdateCart() {
        runBlocking {
            api.redactBucket("cart_id", RequestCart("user_id", "product_id", 2))
        }
    }

    @Test
    fun test9_CreateOrder() {
        runBlocking {
            api.createOrder(RequestOrder("user_id", "product_id", 1))
        }
    }

    @Test
    fun test10_GetProjects() {
        runBlocking {
            api.listProject()
        }
    }

    @Test
    fun test11_CreateProject() {
        runBlocking {
            api.createProject(RequestProject(
                title = "Тестовый проект ${System.currentTimeMillis()}",
                typeProject = "web",
                user_id = "test_user_id",
                dateStart = "2024-01-01",
                dateEnd = "2024-12-31",
                gender = "male",
                description_source = "Тестовое описание",
                category = "development"
            ))
        }
    }

    @Test
    fun test12_GetUserProfile() {
        runBlocking {
            api.viewUser("test_user_id")
        }
    }

    @Test
    fun test13_Logout() {
        runBlocking {
            api.logout("Bearer token123", "auth_id_123")
        }
    }
}