package com.leventgorgu.cryptoinfo.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

/**
 * Provides the per-coin accent color that drives the adaptive/ambient theming.
 * Marquee coins use hand-tuned brand colors; everything else is extracted from the
 * coin's logo with the Palette API so all ~800 coins get a fitting color automatically.
 */
object CoinTheme {

    private val overrides = mapOf(
        "BTC" to 0xFFF7931A.toInt(),
        "ETH" to 0xFF8E97AD.toInt(),
        "USDT" to 0xFF26A17B.toInt(),
        "USDC" to 0xFF2775CA.toInt(),
        "BNB" to 0xFFF3BA2F.toInt(),
        "SOL" to 0xFF9945FF.toInt(),
        "XRP" to 0xFF23A0DB.toInt(),
        "ADA" to 0xFF3468D1.toInt(),
        "DOGE" to 0xFFC2A633.toInt(),
        "TRX" to 0xFFEF0027.toInt()
    )

    fun overrideFor(symbol: String?): Int? = symbol?.let { overrides[it.uppercase()] }

    /** Picks a vivid, representative color from a logo bitmap. */
    fun fromBitmap(bitmap: Bitmap, fallback: Int): Int {
        val palette = Palette.from(bitmap).generate()
        return palette.getVibrantColor(
            palette.getLightVibrantColor(
                palette.getDominantColor(fallback)
            )
        )
    }

    /** Blends [color] over [base] at [ratio] (0..1); used for the subtle ambient tint. */
    fun blend(color: Int, base: Int, ratio: Float): Int =
        ColorUtils.blendARGB(base, color, ratio)

    /** Returns a readable text color (dark or light) for content placed on [background]. */
    fun textOn(background: Int): Int =
        if (ColorUtils.calculateLuminance(background) > 0.5) 0xFF201605.toInt() else Color.WHITE
}
