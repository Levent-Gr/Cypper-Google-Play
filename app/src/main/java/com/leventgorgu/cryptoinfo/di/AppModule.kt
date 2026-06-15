package com.leventgorgu.cryptoinfo.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.leventgorgu.cryptoinfo.api.CoinGeckoAPI
import com.leventgorgu.cryptoinfo.api.CryptoAPI
import com.leventgorgu.cryptoinfo.repo.CryptoRepository
import com.leventgorgu.cryptoinfo.repo.CryptoRepositoryInterface
import com.leventgorgu.cryptoinfo.roomdb.CryptoDao
import com.leventgorgu.cryptoinfo.roomdb.CryptoDatabase
import com.leventgorgu.cryptoinfo.util.Util.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun injectCryptoAPI(@ApplicationContext context: Context): CryptoAPI = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CryptoAPI::class.java)

    @Singleton
    @Provides
    fun injectCoinGeckoAPI(): CoinGeckoAPI = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CoinGeckoAPI::class.java)

    @Singleton
    @Provides
    fun injectNormalRepo(api: CryptoAPI,cryptoDao: CryptoDao) = CryptoRepository(api,cryptoDao) as CryptoRepositoryInterface

    @Singleton
    @Provides
    fun injectRoomDatabase(@ApplicationContext context: Context):CryptoDatabase
    = Room.databaseBuilder(context,CryptoDatabase::class.java,"crypto-database")
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun injectDao(cryptoDatabase: CryptoDatabase)= cryptoDatabase.getCryptoDao()

}