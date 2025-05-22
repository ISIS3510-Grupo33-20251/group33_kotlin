package com.example.universe.di

import android.content.Context
import android.net.ConnectivityManager
import com.example.universe.data.api.AuthApiService
import com.example.universe.data.api.CalculatorApiService
import com.example.universe.data.api.FlashcardApiService
import com.example.universe.data.api.MeetingApiService
import com.example.universe.data.api.UserApiService
import com.example.universe.data.api.NoteApiService
import com.example.universe.data.repositories.NetworkConnectivityObserverImpl
import com.example.universe.domain.repositories.NetworkConnectivityObserver
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ): NetworkConnectivityObserver {
        return NetworkConnectivityObserverImpl(context)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://group33-back.onrender.com") // Replace with backend URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNoteApiService(retrofit: Retrofit): NoteApiService {
        return retrofit.create(NoteApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMeetingApiService(retrofit: Retrofit): MeetingApiService {
        return retrofit.create(MeetingApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFlashcardApiService(retrofit: Retrofit): FlashcardApiService {
        return retrofit.create(FlashcardApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCalculatorApiService(retrofit: Retrofit): CalculatorApiService {
        return retrofit.create(CalculatorApiService::class.java)
    }

}