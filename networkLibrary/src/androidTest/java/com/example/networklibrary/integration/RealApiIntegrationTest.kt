package com.example.networklibrary.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.networklibrary.data.remote.PBApi
import com.example.networklibrary.data.repository.PBRepositoryImpl
import com.example.networklibrary.domain.model.NetworkResult
import com.example.networklibrary.domain.model.RequestAuth
import com.example.networklibrary.domain.model.RequestCart
import com.example.networklibrary.domain.model.RequestOrder
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.networklibrary.domain.model.RequestProject
import com.example.networklibrary.domain.model.RequestRegister
import com.example.networklibrary.domain.model.RequestUser
import com.example.networklibrary.domain.model.UserAuth
import com.example.networklibrary.network.monitor.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class PocketBaseCRUDTest {

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
    //private var globalUserId = ""


    fun createTestUser(): String? = runBlocking {
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

    fun getTestProductId(): String? = runBlocking {
        val productsResult = repository.listProduct()
        return@runBlocking if (productsResult is NetworkResult.Success &&
            productsResult.data.items.isNotEmpty()) {
            productsResult.data.items[0].id // Возвращаем ID первого продукта
        } else {
            null
        }
    }
    private var globalUserId = createTestUser()
    private var globalProductId = getTestProductId()
    
    @Test
    fun testFullUserCRUD() = runBlocking {
        var authToken: String? = null
        var userId: String? = null
        var idTokenForLogout: String? = null
        var listUserAuth: List<UserAuth> = emptyList()

        // 1. РЕГИСТРАЦИЯ
        println("\n1. ТЕСТ РЕГИСТРАЦИИ")
        val registerRequest = RequestRegister(
            email = "test${currentTime}@example.com",
            password = "password123",
            passwordConfirm = "password123"
        )

        val registerResult = repository.registration(registerRequest)
        when (registerResult) {
            is NetworkResult.Success -> {
                println(" Регистрация УСПЕШНА!")
                userId = registerResult.data.id

                println(" User ID: $userId")

                // 2. АВТОРИЗАЦИЯ
                println("\n2. ТЕСТ АВТОРИЗАЦИИ")
                val authRequest = RequestAuth(
                    identity = registerRequest.email,
                    password = registerRequest.password
                )

                val authResult = repository.authorizationUser(authRequest)
                when (authResult) {
                    is NetworkResult.Success -> {
                        println(" Авторизация УСПЕШНА!")
                        authToken = authResult.data.token
                        println("   Токен: ${authToken?.take(20) ?: "N/A"}...")
                        println("   User: test${currentTime}@example.com")

                        // 3. ПОЛУЧЕНИЕ ID_TOKEN ДЛЯ ВЫХОДА через returnIdToken()
                        println("\n3.  ПОЛУЧЕНИЕ ID_TOKEN ДЛЯ ВЫХОДА")
                        val idTokenResult = repository.returnIdToken(authToken)
                        when (idTokenResult) {
                            is NetworkResult.Success -> {
                                println("список ID_TOKEN получен успешно!")
                                println("найдено ID_TOKEN ${idTokenResult.data.items.size}")
                                listUserAuth = idTokenResult.data.items


                                if (listUserAuth.isNotEmpty()) {
                                    for (i in 0..listUserAuth.size - 1) {
                                        if (userId == listUserAuth[i].recordRef)
                                            idTokenForLogout = listUserAuth[i].id

                                        println("   ID Token для входа: ${idTokenForLogout ?: "N/A"}")
                                    }
                                }

                                // 4. ПРОСМОТР ПРОФИЛЯ
                                println("\n4.  ТЕСТ ПРОСМОТРА ПРОФИЛЯ")
                                if (userId != null) {
                                    val viewResult = repository.viewUser(userId!!)
                                    when (viewResult) {
                                        is NetworkResult.Success -> {
                                            println(" Просмотр профиля!")
                                            println("   Email: test${currentTime}@example.com")
                                            println("   ID: ${viewResult.data.id}")


                                            // 5. РЕДАКТИРОВАНИЕ ПРОФИЛЯ
                                            println("\n5.  ТЕСТ РЕДАКТИРОВАНИЯ ПРОФИЛЯ")
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
                                                    println(" Редактирование УСПЕШНО!")
                                                    println(" Имя: ${updateResult.data.firstname}")
                                                    println(" Фамилия: ${updateResult.data.lastname}")

                                                    // 6.  ТЕСТ ВЫХОДА ИЗ СИСТЕМЫ
                                                    println("\n6.  ТЕСТ ВЫХОДА ИЗ СИСТЕМЫ")

                                                    if (idTokenForLogout != null) {
                                                        val logoutResult = repository.logout(
                                                            authToken!!,
                                                            idTokenForLogout!!
                                                        )
                                                        when (logoutResult) {
                                                            is NetworkResult.Success -> {
                                                                println(" Выход УСПЕШЕН!")
                                                                println(" Токен удален из _authOrigins")

                                                                /*// 7. ПРОВЕРКА, ЧТО ТОКЕН БОЛЬШЕ НЕ РАБОТАЕТ
                                                                println("\n7.  ПРОВЕРКА ДОСТУПА ПОСЛЕ ВЫХОДА")
                                                                val checkAccessResult = repository.viewUser(userId)
                                                                when (checkAccessResult) {
                                                                    is NetworkResult.Success -> {
                                                                        println("  Предупреждение: Доступ все еще возможен после выхода")
                                                                    }
                                                                    is NetworkResult.Error -> {
                                                                        println(" Подтверждено: Доступ запрещен после выхода")
                                                                        println("   Ошибка: ${checkAccessResult.error.message}")
                                                                    }
                                                                    else -> println(" Неизвестный статус доступа после выхода")
                                                                }*/
                                                            }

                                                            is NetworkResult.Error -> {

                                                                println("Выход осуществленн,Токен удален из _authOrigins, (возвращаемое значение null) ")
                                                            }

                                                            else -> println(" Неизвестная ошибка выхода")
                                                        }
                                                    } else {
                                                        println(" Невозможно выполнить выход: ID Token не получен")
                                                    }
                                                }

                                                is NetworkResult.Error -> {
                                                    println(" Ошибка редактирования: ${updateResult.error.message}")
                                                }

                                                else -> println(" Неизвестная ошибка редактирования")
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            println(" Ошибка просмотра профиля: ${viewResult.error.message}")
                                        }

                                        else -> println(" Неизвестная ошибка просмотра профиля")
                                    }
                                } else {
                                    println(" Ошибка: User ID не получен")
                                }
                            }

                            is NetworkResult.Error -> {
                                println(" Ошибка получения ID_TOKEN: ${idTokenResult.error.message}")
                            }

                            else -> println(" Неизвестная ошибка получения ID_TOKEN")
                        }
                    }

                    is NetworkResult.Error -> {
                        println(" Ошибка авторизации: ${authResult.error.message}")
                    }

                    else -> println(" Неизвестная ошибка авторизации")
                }
            }

            is NetworkResult.Error -> {
                println(" Ошибка регистрации: ${registerResult.error.message}")
                if (registerResult.error.error400 != null) {
                    println("   Детали: ${registerResult.error.error400.message}")
                }
            }

            else -> println(" Неизвестная ошибка регистрации")
        }
    }

    @Test
    fun testProductCRUD() = runBlocking {
        // 1. ПРОСМОТР ВСЕХ ПРОДУКТОВ
        println("\n1. ТЕСТ ПОЛУЧЕНИЯ ПРОДУКТОВ")

        val productsResult = repository.listProduct()
        when (productsResult) {
            is NetworkResult.Success -> {
                println("   Найдено продуктов: ${productsResult.data.items.size}")

                // Вывод первых 3 продуктов
                productsResult.data.items.take(3).forEachIndexed { index, product ->
                    println(" ${index + 1}. ${product.title} - ${product.price} руб.")

                }

                // 2. ПОИСК ПРОДУКТОВ
                println("\n2.  ТЕСТ ПОИСКА ПРОДУКТОВ")
                val searchResult = repository.listProduct("(title != 'null')")
                when (searchResult) {
                    is NetworkResult.Success -> {
                        println("(title != 'null')")
                        println("   Найдено по запросу: ${searchResult.data.items.size}")
                    }

                    else -> println(" Ошибка поиска")
                }

                // 3. ПРОСМОТР ОПИСАНИЯ ПРОДУКТА (если есть продукты)
                if (productsResult.data.items.isNotEmpty()) {
                    println("\n3.  ТЕСТ ПРОСМОТРА ОПИСАНИЯ ПРОДУКТА")
                    val firstProductId = productsResult.data.items[0].id
                    val descriptionResult = repository.descriptionProduct(firstProductId)
                    when (descriptionResult) {
                        is NetworkResult.Success -> {
                            println("   Описание продукта получено!")
                            println("   Название: ${descriptionResult.data.title}")
                            println("   Цена: ${descriptionResult.data.price}")
                            if (descriptionResult.data.description != null) {
                                println("   Описание: ${descriptionResult.data.description.take(100)}...")
                            }
                        }

                        else -> println("  Ошибка получения описания")
                    }
                }
            }

            is NetworkResult.Error -> {
                println("  Ошибка получения продуктов: ${productsResult.error.message}")
            }

            else -> println("  Неизвестная ошибка получения продуктов")
        }
    }

    @Test
    fun testProjectCRUD() = runBlocking {
        // 1. ПРОСМОТР ВСЕХ ПРОЕКТОВ
        val projectsResult = repository.listProject()
        when (projectsResult) {
            is NetworkResult.Success -> {
                println("Проекты получены!")
                println("Найдено проектов: ${projectsResult.data.items.size}")

                println("Перывые 3")
                projectsResult.data.items.take(3).forEachIndexed { index, project ->
                    println("   ${index + 1}. ${project.title}")
                }

                // 2. СОЗДАНИЕ НОВОГО ПРОЕКТА
                println("\n2. ТЕСТ СОЗДАНИЯ ПРОЕКТА")
                val newProjectRequest = RequestProject(
                    title = "Тестовый проект ${System.currentTimeMillis()}",
                    typeProject = "web",
                    user_id = globalUserId!!,
                    dateStart = "2024-01-01",
                    dateEnd = "2024-12-31",
                    gender = "unisex",
                    description_source = "Это тестовое описание проекта",
                    category = "development",
                    //image = null
                )

                val createResult = repository.createProject(newProjectRequest)
                when (createResult) {
                    is NetworkResult.Success -> {
                        println(" Проект создан!")
                        println(" ID проекта: ${createResult.data.id}")
                        println(" ID проекта: ${createResult.data.user_id}")
                        println(" Название: ${createResult.data.title}")
                    }

                    is NetworkResult.Error -> {
                        println(" Ошибка создания проекта: ${createResult.error.message}")
                        if (createResult.error.error400 != null) {
                            println("   Детали: ${createResult.error.error400.message}")
                        }
                    }

                    else -> println(" Неизвестная ошибка создания проекта")
                }
            }

            is NetworkResult.Error -> {
                println(" Ошибка получения проектов: ${projectsResult.error.message}")
            }

            else -> println(" Неизвестная ошибка получения проектов")
        }
    }

    @Test
    fun testCartAndOrderCRUD() = runBlocking {

        val userId = globalUserId!!
        val productId = globalProductId!!

        // 1. СОЗДАНИЕ КОРЗИНЫ
        println("\n1. ТЕСТ СОЗДАНИЯ КОРЗИНЫ")
        val cartRequest = RequestCart(
            user_id = userId,
            product_id = productId,
            count = 2
        )

        val cartResult = repository.createBucket(cartRequest)
        when (cartResult) {
            is NetworkResult.Success -> {
                println("Корзина создана!")
                println("ID корзины: ${cartResult.data.id}")
                println("Товаров: ${cartResult.data.count}")

                // 2. РЕДАКТИРОВАНИЕ КОРЗИНЫ
                println("\n2.  ТЕСТ РЕДАКТИРОВАНИЯ КОРЗИНЫ")
                val updateCartRequest = RequestCart(
                    user_id = cartResult.data.user_id,
                    product_id = cartResult.data.product_id,
                    count = 5 // Увеличиваем количество
                )

                val updateCartResult =
                    repository.redactBucket(cartResult.data.id, updateCartRequest)
                when (updateCartResult) {
                    is NetworkResult.Success -> {
                        println("Корзина обновлена!")
                        println("Новое количество: ${updateCartResult.data.count}")

                        // 3. СОЗДАНИЕ ЗАКАЗА
                        println("\n3.   ТЕСТ СОЗДАНИЯ ЗАКАЗА")
                        val orderRequest = RequestOrder(
                            user_id = updateCartResult.data.user_id,
                            product_id = updateCartResult.data.product_id,
                            count = updateCartResult.data.count
                        )

                        val orderResult = repository.createOrder(orderRequest)
                        when (orderResult) {
                            is NetworkResult.Success -> {
                                println(" Заказ создан!")
                                println(" ID заказа: ${orderResult.data.id}")
                                println(" Статус: создан")
                            }

                            is NetworkResult.Error -> {
                                println("Ошибка создания заказа: ${orderResult.error.message}")
                            }

                            else -> println("Неизвестная ошибка создания заказа")
                        }
                    }

                    is NetworkResult.Error -> {
                        println("Ошибка обновления корзины: ${updateCartResult.error.message}")
                    }

                    else -> println("  Неизвестная ошибка обновления корзины")
                }
            }

            is NetworkResult.Error -> {
                println("  Ошибка создания корзины: ${cartResult.error.message}")
            }

            else -> println("  Неизвестная ошибка создания корзины")
        }
    }

    @Test
    fun testNewsCRUD() = runBlocking {
        println("\n1. ТЕСТ ПОЛУЧЕНИЯ НОВОСТЕЙ И АКЦИЙ")
        val newsResult = repository.promoAndNews()
        when (newsResult) {
            is NetworkResult.Success -> {
                println("   Найдено записей: ${newsResult.data.items.size}")

                if (newsResult.data.items.isEmpty()) {
                    println(" Коллекция новостей пустая")
                } else {
                    println("   Всего страниц: ${newsResult.data.totalPages}")
                    println("   Всего записей: ${newsResult.data.totalItems}")
                    newsResult.data.items.forEachIndexed { index, news ->
                        println("   ${index + 1}. ID: ${news.id}")
                        if (news.newsImage != null) {
                            println(" Изображение: ${news.newsImage}")
                        }
                        println(" Создано: ${news.created}")
                    }
                }
            }

            is NetworkResult.Error -> {
                println(" Ошибка получения новостей: ${newsResult.error.message}")
            }

            else -> println(" Неизвестная ошибка получения новостей")
        }
    }
}