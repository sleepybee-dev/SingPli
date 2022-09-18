package com.sleepybee.singpli.di

import com.sleepybee.singpli.network.YTService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECTION_TIMEOUT: Long = 5000
    private const val READ_TIMEOUT: Long = 15000

    private const val BASE_URL_YOUTUBE = "https://www.googleapis.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(HttpLoggingInterceptor())
//            .addNetworkInterceptor()
            .build()
    }

    @Provides  //인스턴스 삽입
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL_YOUTUBE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideYTService(retrofit: Retrofit): YTService {
        return retrofit.create(YTService::class.java)
    }

}