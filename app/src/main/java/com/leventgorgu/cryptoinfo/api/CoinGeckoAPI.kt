package com.leventgorgu.cryptoinfo.api

import com.leventgorgu.cryptoinfo.model.coingecko.MarketChart
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// CoinGecko public API (free, no key) — used for historical price charts.
interface CoinGeckoAPI {

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: Int = 7
    ): Response<MarketChart>
}
