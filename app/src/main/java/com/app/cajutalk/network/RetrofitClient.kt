package com.app.cajutalk.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // MUITO IMPORTANTE: Substitua pela URL base da sua API
    // Se API em localhost e testando no EMULADOR:
    private const val BASE_URL = "https://cajutalkapi.onrender.com/"

    // Interceptor para logging (opcional, mas MUITO útil para debug)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Loga o corpo da requisição e resposta
    }

    // Cliente OkHttp customizado (para timeouts, interceptors, etc.)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Adiciona o logging interceptor
        // Você pode adicionar um interceptor de autenticação aqui também
        // .addInterceptor(AuthInterceptor("SEU_TOKEN_AQUI")) // Exemplo
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Usa o OkHttpClient customizado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}

// Exemplo de AuthInterceptor (coloque em um arquivo separado ou aqui)
// class AuthInterceptor(private val authToken: String) : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val originalRequest = chain.request()
//        val requestBuilder = originalRequest.newBuilder()
//            .header("Authorization", "Bearer $authToken")
//        val request = requestBuilder.build()
//        return chain.proceed(request)
//    }
// }