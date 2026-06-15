package com.leventgorgu.cryptoinfo.model.coingecko

// CoinGecko /market_chart returns prices as [ [timestampMs, price], ... ]
data class MarketChart(
    val prices: List<List<Double>>
)
