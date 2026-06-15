package com.leventgorgu.cryptoinfo.util

/**
 * Maps a coin symbol to its CoinGecko id (slug), which the CoinGecko chart endpoint
 * requires. Covers the most-viewed coins; unknown symbols return null and the detail
 * chart is simply hidden (it is non-critical).
 */
object GeckoIds {

    private val map = mapOf(
        "BTC" to "bitcoin",
        "ETH" to "ethereum",
        "USDT" to "tether",
        "BNB" to "binancecoin",
        "SOL" to "solana",
        "XRP" to "ripple",
        "USDC" to "usd-coin",
        "ADA" to "cardano",
        "DOGE" to "dogecoin",
        "TRX" to "tron",
        "TON" to "the-open-network",
        "DOT" to "polkadot",
        "MATIC" to "matic-network",
        "LTC" to "litecoin",
        "SHIB" to "shiba-inu",
        "AVAX" to "avalanche-2",
        "LINK" to "chainlink",
        "BCH" to "bitcoin-cash",
        "XLM" to "stellar",
        "UNI" to "uniswap",
        "ATOM" to "cosmos",
        "XMR" to "monero",
        "ETC" to "ethereum-classic",
        "FIL" to "filecoin",
        "APT" to "aptos",
        "NEAR" to "near",
        "ARB" to "arbitrum",
        "OP" to "optimism",
        "VET" to "vechain",
        "ALGO" to "algorand",
        "AAVE" to "aave",
        "DAI" to "dai",
        "LEO" to "leo-token",
        "HBAR" to "hedera-hashgraph",
        "ICP" to "internet-computer"
    )

    fun forSymbol(symbol: String?): String? = symbol?.let { map[it.uppercase()] }
}
