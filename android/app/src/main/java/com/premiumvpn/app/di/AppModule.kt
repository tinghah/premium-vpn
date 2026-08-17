package com.premiumvpn.app.di

import android.content.Context
import androidx.room.Room
import com.premiumvpn.app.data.local.AppDatabase
import com.premiumvpn.app.data.local.KeyDao
import com.premiumvpn.app.data.remote.OutlineApiService
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
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "premium_vpn.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideKeyDao(database: AppDatabase): KeyDao = database.keyDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideOutlineApiService(client: OkHttpClient): OutlineApiService {
        // Base URL is overridden per-request using the full URL from the key
        return Retrofit.Builder()
            .baseUrl("https://placeholder.example.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OutlineApiService::class.java)
    }
}
