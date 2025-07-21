package com.jarrod.house.data.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jarrod.house.data.datastore.dataStore
import com.jarrod.house.data.datastore.DataStoreKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Cambiar a localhost para desarrollo local
    private const val BASE_URL_PROD = "https://housemeter-backend-production.up.railway.app/api/"
    private const val BASE_URL_DEV = "http://10.0.2.2:3000/api/"
    
    // Usar producción por defecto
    private const val BASE_URL = BASE_URL_PROD

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .disableHtmlEscaping()
        .create()

    fun getApiService(context: Context): ApiService {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")

                // Add authorization header if token exists
                try {
                    val token = runBlocking {
                        context.dataStore.data.first()[DataStoreKeys.TOKEN_KEY]
                    }
                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                } catch (e: Exception) {
                    // Continue without token if there's an error
                }

                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    // Backward compatibility - deprecated
    @Deprecated("Use getApiService(context) instead")
    val apiService: ApiService by lazy {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}