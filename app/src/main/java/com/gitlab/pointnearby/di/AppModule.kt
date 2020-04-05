package com.gitlab.pointnearby.di

import android.content.Context
import com.gitlab.pointnearby.PointNearbyApp
import com.gitlab.pointnearby.data.remote.RequestService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule {
    private val connectionTimeout: Long = 50
    private val readTimeout: Long = 50
    private val writeTimeout: Long = 50

    @Provides
    fun providesContext(application: PointNearbyApp): Context{
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideRequestService(): RequestService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
        return Retrofit.Builder()
            .client(client)
            .baseUrl("https://olsox.000webhostapp.com/point-nearby/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(RequestService::class.java)
    }
}