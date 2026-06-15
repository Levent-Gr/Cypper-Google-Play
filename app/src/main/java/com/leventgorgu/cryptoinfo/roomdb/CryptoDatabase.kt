package com.leventgorgu.cryptoinfo.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CryptoEntity::class,CryptoDetailEntity::class,CryptoFavoriteEntity::class], version = 2)
abstract class CryptoDatabase :RoomDatabase() {
    abstract fun getCryptoDao(): CryptoDao
}