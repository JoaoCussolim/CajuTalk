package com.app.cajutalk.network

import android.content.Context
import com.app.cajutalk.network.models.RefreshTokenRequest
import com.app.cajutalk.network.util.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://cajutalkapi.onrender.com/"

    private var internalAuthService: ApiService? = null

    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Ou .HEADERS, .BASIC, .NONE
        }
    }

    private fun getAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val accessToken = TokenManager.getAccessToken(context.applicationContext)

            val requestBuilder = originalRequest.newBuilder()
            if (accessToken != null) {
                requestBuilder.header("Authorization", "Bearer $accessToken")
            }

            chain.proceed(requestBuilder.build())
        }
    }

    private fun getAuthenticator(context: Context, retrofit: Retrofit): Authenticator {
        return object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                val refreshToken = TokenManager.getRefreshToken(context.applicationContext)
                if (refreshToken == null) {
                    // Não há refresh token, não tentar autenticar
                    TokenManager.clearTokens(context.applicationContext) // Limpar tokens se não houver refresh
                    // Aqui você pode querer navegar para a tela de login
                    return null
                }

                // Usar o serviço de autenticação para renovar o token SINCRONAMENTE
                // O AuthService deve ser obtido da instância Retrofit principal, mas
                // sem o próprio authenticator para evitar loop infinito.
                // Para simplificar, vamos assumir que o internalAuthService já foi inicializado.
                // Uma abordagem mais robusta envolveria criar uma instância Retrofit separada
                // para a chamada de refresh ou injetar o authService.

                val authServiceForRefresh = retrofit.newBuilder()
                    .client(OkHttpClient.Builder() // Cliente OkHttp SEM o Authenticator para a chamada de refresh
                        .addInterceptor(getLoggingInterceptor()) // Logar a chamada de refresh também
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    )
                    .build()
                    .create(ApiService::class.java)


                synchronized(this) { // Sincronizar para evitar múltiplas chamadas de refresh
                    // Verificar se o token já foi atualizado por outra thread
                    val currentAccessToken = TokenManager.getAccessToken(context.applicationContext)
                    if (currentAccessToken != null && response.request.header("Authorization") != "Bearer $currentAccessToken") {
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer $currentAccessToken")
                            .build()
                    }

                    try {
                        val refreshCall = authServiceForRefresh.refreshTokenSync(RefreshTokenRequest(refreshToken))
                        val tokenResponse = refreshCall.execute() // Chamada SÍNCRONA

                        if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                            val newTokens = tokenResponse.body()!!
                            TokenManager.saveTokens(
                                context.applicationContext,
                                newTokens.AccessToken,
                                newTokens.RefreshToken
                            )
                            // Tentar novamente a requisição original com o novo token
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer ${newTokens.AccessToken}")
                                .build()
                        } else {
                            // Falha ao renovar, limpar tokens e não tentar novamente
                            TokenManager.clearTokens(context.applicationContext)
                            // Aqui você pode querer navegar para a tela de login
                        }
                    } catch (e: Exception) {
                        // Tratar exceção da chamada de refresh
                        TokenManager.clearTokens(context.applicationContext)
                        e.printStackTrace()
                    }
                    return null // Falha na autenticação
                }
            }
        }
    }


    private fun provideOkHttpClient(context: Context, retrofitInstanceForAuthenticator: () -> Retrofit): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(getLoggingInterceptor())
            .addInterceptor(getAuthInterceptor(context.applicationContext))
            .authenticator(getAuthenticator(context.applicationContext, retrofitInstanceForAuthenticator())) // Passar a instância Retrofit
            .build()
    }

    // Usar lazy para garantir que a instância Retrofit seja criada apenas uma vez
    // e de forma thread-safe para a referência mútua com o Authenticator.
    private val retrofitInstance: (Context) -> Retrofit by lazy {
        var retrofit: Retrofit? = null
        val clientProvider: (Context) -> OkHttpClient = { ctx ->
            provideOkHttpClient(ctx) {
                // Esta lambda é chamada pelo provideOkHttpClient para obter a instância Retrofit
                // que será usada pelo Authenticator.
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                        .client(provideOkHttpClient(ctx) { retrofit!! }) // Evitar recursão aqui é complexo, simplificando
                        // Para o authenticator, pode-se criar um OkHttp sem o authenticator
                        // ou garantir que a chamada de refresh não use o authenticator.
                        // A solução no getAuthenticator já cria um OkHttp dedicado.
                        .build()
                }
                retrofit!!
            }
        }

        // Função que será retornada pelo lazy delegate
        { applicationContext: Context ->
            if (retrofit == null) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(getLoggingInterceptor())
                    .addInterceptor(getAuthInterceptor(applicationContext))
                    // O Authenticator precisa de uma instância Retrofit para chamar o refresh.
                    // Vamos construir o OkHttpClient com o Authenticator que usa uma referência
                    // tardia para o Retrofit.
                    .authenticator(object : Authenticator {
                        override fun authenticate(route: Route?, response: Response): Request? {
                            val refreshToken = TokenManager.getRefreshToken(applicationContext)
                                ?: run { TokenManager.clearTokens(applicationContext); return null }

                            // Criar um cliente OkHttp e Retrofit *separados* para a chamada de refresh
                            // para evitar loops de autenticação.
                            val refreshOkHttpClient = OkHttpClient.Builder()
                                .addInterceptor(getLoggingInterceptor())
                                .connectTimeout(15, TimeUnit.SECONDS)
                                .readTimeout(15, TimeUnit.SECONDS)
                                .build()

                            val refreshRetrofit = Retrofit.Builder()
                                .baseUrl(BASE_URL)
                                .client(refreshOkHttpClient)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()

                            val authServiceForRefresh = refreshRetrofit.create(ApiService::class.java)

                            synchronized(this) {
                                val currentAccessToken = TokenManager.getAccessToken(applicationContext)
                                if (currentAccessToken != null && response.request.header("Authorization") != "Bearer $currentAccessToken") {
                                    return response.request.newBuilder()
                                        .header("Authorization", "Bearer $currentAccessToken")
                                        .build()
                                }

                                try {
                                    val refreshCall = authServiceForRefresh.refreshTokenSync(
                                        RefreshTokenRequest(refreshToken)
                                    )
                                    val tokenResponse = refreshCall.execute()

                                    if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                                        val newTokens = tokenResponse.body()!!
                                        TokenManager.saveTokens(applicationContext, newTokens.AccessToken, newTokens.RefreshToken)
                                        return response.request.newBuilder()
                                            .header("Authorization", "Bearer ${newTokens.AccessToken}")
                                            .build()
                                    } else {
                                        TokenManager.clearTokens(applicationContext)
                                    }
                                } catch (e: Exception) {
                                    TokenManager.clearTokens(applicationContext)
                                    e.printStackTrace()
                                }
                                return null
                            }
                        }
                    })
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Usar o OkHttpClient com o Authenticator configurado
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
                    .build()
            }
            retrofit!!
        }
    }


    fun <S> createService(context: Context, serviceClass: Class<S>): S {
        return retrofitInstance(context.applicationContext).create(serviceClass)
    }
}

// Para uso simplificado, você pode criar uma instância do serviço diretamente.
// Este é um exemplo, idealmente você usaria injeção de dependência (Hilt).
// object ApiClient {
//     fun getApiService(context: Context): ApiService {
//         return RetrofitClient.createService(context, ApiService::class.java)
//     }
// }