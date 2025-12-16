package com.example.networklibrary.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.networklibrary.data.remote.PBApi
import com.example.networklibrary.data.repository.PBRepositoryImpl
import com.example.networklibrary.domain.model.*
import com.example.networklibrary.network.monitor.NetworkMonitor
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class ShortTestNetworkLibrary {

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
    private var globalUserId: String? = null
    private var globalProductId: String? = null

    @Before
    fun setUp() = runBlocking {
        globalUserId = createTestUser()
        globalProductId = getTestProductId()
    }

    private suspend fun createTestUser(): String? {
        return repository.registration(
            RequestRegister(
                email = "test2${currentTime}@example.com",
                password = "password123",
                passwordConfirm = "password123",
                "tEST",
                "tEST",
                "tEST",
                "tEST",
                "tEST"
            )
        ).let { result ->
            if (result is NetworkResult.Success) result.data.id else null
        }
    }

    private suspend fun getTestProductId(): String? {
        return repository.listProduct().let { result ->
            if (result is NetworkResult.Success && result.data.items.isNotEmpty()) {
                result.data.items[0].id
            } else {
                null
            }
        }
    }

    private inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Success) action(data)
        return this
    }

    private inline fun <T> NetworkResult<T>.onError(action: (String) -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Error) action(error.message ?: "Неизвестная ошибка")
        return this
    }

    @Test
    fun testFullUserCRUD() {
        runBlocking {
            var authToken: String? = null
            var userId: String? = null
            var idTokenForLogout: String? = null

            val registerRequest = RequestRegister(
                email = "test${currentTime}@example.com",
                password = "password123",
                passwordConfirm = "password123",
                "tEST",
                "tEST",
                "tEST",
                "tEST",
                "tEST"
            )

            repository.registration(registerRequest)
                .onSuccess {
                    userId = it.id
                    println("Регистрация: УСПЕШНО, ID: ${userId?.take(10)}")
                }
                .onError { println("Регистрация: ОШИБКА - $it") }

            userId?.let { uid ->
                repository.authorizationUser(RequestAuth(registerRequest.email, registerRequest.password))
                    .onSuccess {
                        authToken = it.token
                        println("Авторизация: УСПЕШНО, Токен: ${authToken?.take(10)}")
                    }
                    .onError { println("Авторизация: ОШИБКА - $it") }

                authToken?.let { token ->
                    repository.returnIdToken(token)
                        .onSuccess {
                            idTokenForLogout = it.items.firstOrNull { auth -> auth.recordRef == uid }?.id
                            println("ID токены: ${it.items.size}, Токен для выхода: ${idTokenForLogout?.take(10)}")
                        }
                        .onError { println("Получение ID токенов: ОШИБКА - $it") }

                    repository.viewUser(uid)
                        .onSuccess { user ->
                            println("Просмотр профиля: УСПЕШНО, Id: ${user.id}")

                            repository.redactProfile(uid, RequestUser(
                                email = registerRequest.email,
                                emailVisibility = true,
                                firstname = "Игорь",
                                lastname = "Тестов",
                                secondname = "Петрович",
                                datebirthday = "1990-01-01",
                                gender = "male"
                            )).onSuccess {
                                println("Редактирование профиля: УСПЕШНО, Имя: ${it.firstname}")

                                idTokenForLogout?.let { idToken ->
                                    repository.logout(token, idToken)
                                        .onSuccess { println("Выход из системы: УСПЕШНО") }
                                        .onError { println("Выход из системы: ВЫПОЛНЕН") }
                                }
                            }.onError { println("Редактирование профиля: ОШИБКА - $it") }
                        }.onError { println("Просмотр профиля: ОШИБКА - $it") }
                }
            }
        }
    }

    @Test
    fun testProductCRUD() {
        runBlocking {
            repository.listProduct()
                .onSuccess { products ->
                    println("Всего продуктов: ${products.items.size}")

                    if (products.items.isNotEmpty()) {
                        products.items.take(3).forEachIndexed { i, p ->
                            println("Продукт ${i + 1}: ${p.title} - ${p.price} руб.")
                        }

                        repository.descriptionProduct(products.items[0].id)
                            .onSuccess { println("Описание продукта: ${it.title}, ${it.price} руб.") }
                            .onError { println("Описание продукта: ОШИБКА - $it") }
                    }

                    repository.listProduct("(title != 'null')")
                        .onSuccess { println("Отфильтровано продуктов: ${it.items.size}") }
                        .onError { println("Поиск продуктов: ОШИБКА - $it") }
                }
                .onError { println("Список продуктов: ОШИБКА - $it") }
        }
    }

    @Test
    fun testProjectCRUD() {
        runBlocking {
            repository.listProject()
                .onSuccess { projects ->
                    println("Всего проектов: ${projects.items.size}")

                    if (projects.items.isNotEmpty()) {
                        projects.items.take(3).forEachIndexed { i, p ->
                            println("Проект ${i + 1}: ${p.title}")
                        }
                    }

                    globalUserId?.let { userId ->
                        repository.createProject(RequestProject(
                            title = "Тестовый проект ${System.currentTimeMillis()}",
                            typeProject = "web",
                            user_id = userId,
                            dateStart = "2024-01-01",
                            dateEnd = "2024-12-31",
                            gender = "unisex",
                            description_source = "Тестовое описание",
                            category = "development"
                        )).onSuccess { println("Проект создан: ${it.id}, ${it.title}") }
                            .onError { println("Создание проекта: ОШИБКА - $it") }
                    }
                }
                .onError { println("Список проектов: ОШИБКА - $it") }
        }
    }

    @Test
    fun testCartAndOrderCRUD() {
        runBlocking {
            if (globalUserId == null || globalProductId == null) {
                println("Отсутствует ID пользователя или продукта")
                return@runBlocking
            }

            repository.createBucket(RequestCart(globalUserId!!, globalProductId!!, 2))
                .onSuccess { cart ->
                    println("Корзина создана: ${cart.id}, Количество: ${cart.count}")

                    repository.redactBucket(cart.id, RequestCart(cart.user_id, cart.product_id, 5))
                        .onSuccess { updatedCart ->
                            println("Корзина обновлена: ${updatedCart.count}")

                            repository.createOrder(RequestOrder(updatedCart.user_id, updatedCart.product_id, updatedCart.count))
                                .onSuccess { println("Заказ создан: ${it.id}") }
                                .onError { println("Создание заказа: ОШИБКА - $it") }
                        }
                        .onError { println("Обновление корзины: ОШИБКА - $it") }
                }
                .onError { println("Создание корзины: ОШИБКА - $it") }
        }
    }

    @Test
    fun testNewsCRUD() {
        runBlocking {
            repository.promoAndNews()
                .onSuccess {
                    println("Новостей всего: ${it.items.size}, Страниц: ${it.totalPages}")
                }
                .onError { println("Новости: ОШИБКА - $it") }
        }
    }
}