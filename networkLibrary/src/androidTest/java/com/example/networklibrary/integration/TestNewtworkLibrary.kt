package com.example.networklibrary.integration


import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.networklibrary.data.remote.PBApi
import com.example.networklibrary.data.repository.PBRepositoryImpl
import com.example.networklibrary.domain.model.*
import com.example.networklibrary.network.monitor.NetworkMonitor
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class TestNewtworkLibrary {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8090/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val testApi = retrofit.create(PBApi::class.java)

    private val networkMonitor = object : NetworkMonitor {
        override fun isConnected(): Boolean = true
    }

    private val repository = PBRepositoryImpl(testApi, networkMonitor)

    private val currentTime = System.currentTimeMillis().toString()
    private var globalUserId = createTestUser()
    private var globalProductId = getTestProductId()

    private fun createTestUser(): String? = runBlocking {
        val email = "test2${currentTime}@example.com"
        val registerResult = repository.registration(
            RequestRegister(
                email = email,
                password = "password123",
                passwordConfirm = "password123"
            )
        )
        return@runBlocking if (registerResult is NetworkResult.Success) {
            registerResult.data.id
        } else {
            null
        }
    }

    private fun getTestProductId(): String? = runBlocking {
        val productsResult = repository.listProduct()
        return@runBlocking if (productsResult is NetworkResult.Success &&
            productsResult.data.items.isNotEmpty()) {
            productsResult.data.items[0].id
        } else {
            null
        }
    }

    @Test
    fun testFullUserCRUD() = runBlocking {
        var authToken: String? = null
        var userId: String? = null
        var idTokenForLogout: String? = null
        var listUserAuth: List<UserAuth> = emptyList()

        val registerRequest = RequestRegister(
            email = "test${currentTime}@example.com",
            password = "password123",
            passwordConfirm = "password123"
        )

        val registerResult = repository.registration(registerRequest)
        when (registerResult) {
            is NetworkResult.Success -> {
                userId = registerResult.data.id

                val authRequest = RequestAuth(
                    identity = registerRequest.email,
                    password = registerRequest.password
                )

                val authResult = repository.authorizationUser(authRequest)
                when (authResult) {
                    is NetworkResult.Success -> {
                        authToken = authResult.data.token

                        val idTokenResult = repository.returnIdToken(authToken)
                        when (idTokenResult) {
                            is NetworkResult.Success -> {
                                listUserAuth = idTokenResult.data.items

                                if (listUserAuth.isNotEmpty()) {
                                    for (i in 0..listUserAuth.size - 1) {
                                        if (userId == listUserAuth[i].recordRef)
                                            idTokenForLogout = listUserAuth[i].id
                                    }
                                }

                                if (userId != null) {
                                    val viewResult = repository.viewUser(userId!!)
                                    when (viewResult) {
                                        is NetworkResult.Success -> {
                                            val updateRequest = RequestUser(
                                                email = "test${currentTime}@example.com",
                                                emailVisibility = true,
                                                firstname = "Игорь",
                                                lastname = "Тестов",
                                                secondname = "Петрович",
                                                datebirthday = "1990-01-01",
                                                gender = "male"
                                            )

                                            val updateResult = repository.redactProfile(
                                                userId,
                                                updateRequest
                                            )
                                            when (updateResult) {
                                                is NetworkResult.Success -> {
                                                    if (idTokenForLogout != null) {
                                                        val logoutResult = repository.logout(
                                                            authToken!!,
                                                            idTokenForLogout!!
                                                        )
                                                        when (logoutResult) {
                                                            is NetworkResult.Success -> {
                                                                println("Выход выполнен успешно")
                                                            }

                                                            is NetworkResult.Error -> {
                                                                println("Токен удален")
                                                            }

                                                            else -> println("Ошибка выхода")
                                                        }
                                                    }
                                                }

                                                is NetworkResult.Error -> {
                                                    println("Ошибка редактирования")
                                                }

                                                else -> println("Ошибка редактирования")
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            println("Ошибка просмотра профиля")
                                        }

                                        else -> println("Ошибка просмотра профиля")
                                    }
                                }
                            }

                            is NetworkResult.Error -> {
                                println("Ошибка получения ID_TOKEN")
                            }

                            else -> println("Ошибка получения ID_TOKEN")
                        }
                    }

                    is NetworkResult.Error -> {
                        println("Ошибка авторизации")
                    }

                    else -> println("Ошибка авторизации")
                }
            }

            is NetworkResult.Error -> {
                println("Ошибка регистрации")
            }

            else -> println("Ошибка регистрации")
        }
    }

    @Test
    fun testProductCRUD() = runBlocking {
        val productsResult = repository.listProduct()
        when (productsResult) {
            is NetworkResult.Success -> {
                println("Найдено продуктов: ${productsResult.data.items.size}")

                productsResult.data.items.take(3).forEachIndexed { index, product ->
                    println("${index + 1}. ${product.title} - ${product.price}")
                }

                val searchResult = repository.listProduct("(title != 'null')")
                when (searchResult) {
                    is NetworkResult.Success -> {
                        println("Найдено по запросу: ${searchResult.data.items.size}")
                    }

                    else -> println("Ошибка поиска")
                }

                if (productsResult.data.items.isNotEmpty()) {
                    val firstProductId = productsResult.data.items[0].id
                    val descriptionResult = repository.descriptionProduct(firstProductId)
                    when (descriptionResult) {
                        is NetworkResult.Success -> {
                            println("Название: ${descriptionResult.data.title}")
                            println("Цена: ${descriptionResult.data.price}")
                        }

                        else -> println("Ошибка получения описания")
                    }
                }
            }

            is NetworkResult.Error -> {
                println("Ошибка получения продуктов")
            }

            else -> println("Ошибка получения продуктов")
        }
    }

    @Test
    fun testProjectCRUD() = runBlocking {
        val projectsResult = repository.listProject()
        when (projectsResult) {
            is NetworkResult.Success -> {
                println("Найдено проектов: ${projectsResult.data.items.size}")

                projectsResult.data.items.take(3).forEachIndexed { index, project ->
                    println("${index + 1}. ${project.title}")
                }

                val newProjectRequest = RequestProject(
                    title = "Тестовый проект ${System.currentTimeMillis()}",
                    typeProject = "web",
                    user_id = globalUserId!!,
                    dateStart = "2024-01-01",
                    dateEnd = "2024-12-31",
                    gender = "unisex",
                    description_source = "Это тестовое описание проекта",
                    category = "development"
                )

                val createResult = repository.createProject(newProjectRequest)
                when (createResult) {
                    is NetworkResult.Success -> {
                        println("Проект создан: ${createResult.data.id}")
                        println("Название: ${createResult.data.title}")
                    }

                    is NetworkResult.Error -> {
                        println("Ошибка создания проекта")
                    }

                    else -> println("Ошибка создания проекта")
                }
            }

            is NetworkResult.Error -> {
                println("Ошибка получения проектов")
            }

            else -> println("Ошибка получения проектов")
        }
    }

    @Test
    fun testCartAndOrderCRUD() = runBlocking {
        val userId = globalUserId!!
        val productId = globalProductId!!

        val cartRequest = RequestCart(
            user_id = userId,
            product_id = productId,
            count = 2
        )

        val cartResult = repository.createBucket(cartRequest)
        when (cartResult) {
            is NetworkResult.Success -> {
                println("Корзина создана: ${cartResult.data.id}")

                val updateCartRequest = RequestCart(
                    user_id = cartResult.data.user_id,
                    product_id = cartResult.data.product_id,
                    count = 5
                )

                val updateCartResult = repository.redactBucket(cartResult.data.id, updateCartRequest)
                when (updateCartResult) {
                    is NetworkResult.Success -> {
                        println("Корзина обновлена: ${updateCartResult.data.count}")

                        val orderRequest = RequestOrder(
                            user_id = updateCartResult.data.user_id,
                            product_id = updateCartResult.data.product_id,
                            count = updateCartResult.data.count
                        )

                        val orderResult = repository.createOrder(orderRequest)
                        when (orderResult) {
                            is NetworkResult.Success -> {
                                println("Заказ создан: ${orderResult.data.id}")
                            }

                            is NetworkResult.Error -> {
                                println("Ошибка создания заказа")
                            }

                            else -> println("Ошибка создания заказа")
                        }
                    }

                    is NetworkResult.Error -> {
                        println("Ошибка обновления корзины")
                    }

                    else -> println("Ошибка обновления корзины")
                }
            }

            is NetworkResult.Error -> {
                println("Ошибка создания корзины")
            }

            else -> println("Ошибка создания корзины")
        }
    }

    @Test
    fun testNewsCRUD() = runBlocking {
        val newsResult = repository.promoAndNews()
        when (newsResult) {
            is NetworkResult.Success -> {
                println("Найдено записей: ${newsResult.data.items.size}")
                println("Всего страниц: ${newsResult.data.totalPages}")
            }

            is NetworkResult.Error -> {
                println("Ошибка получения новостей")
            }

            else -> println("Ошибка получения новостей")
        }
    }
}