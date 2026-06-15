package com.leventgorgu.cryptoinfo.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cryptos")
data class CryptoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "Name")
    val name: String,
    @ColumnInfo(name = "Crypto_Symbol")
    val symbol: String,
    @ColumnInfo(name = "CoinMarketCap_Rank")
    val cmcRank: Int,
    @ColumnInfo(name = "Price_USD")
    val price :String,
    @ColumnInfo(name = "Platform")
    val platform:String,
    @ColumnInfo(name = "Change_24h")
    val changePercent24h: Double = 0.0
){

}