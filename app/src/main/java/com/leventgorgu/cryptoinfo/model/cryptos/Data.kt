package com.leventgorgu.cryptoinfo.model.cryptos

// Fields that CoinMarketCap may omit or return as null (e.g. max_supply for coins
// without a cap) are nullable so Gson does not throw and drop the whole list.
data class Data(
    val circulating_supply: Double?,
    val cmc_rank: Int?,//*
    val date_added: String?,
    val id: Int,//*
    val last_updated: String?,
    val max_supply: Double?,
    val name: String,//*
    val num_market_pairs: Int?,
    val platform: Platform?,//*
    val quote: Quote,//*
    val self_reported_circulating_supply: Any?,
    val self_reported_market_cap: Any?,
    val slug: String?,
    val symbol: String,//*
    val tags: List<String>?,
    val total_supply: Double?,//*
    val tvl_ratio: Any?
)
