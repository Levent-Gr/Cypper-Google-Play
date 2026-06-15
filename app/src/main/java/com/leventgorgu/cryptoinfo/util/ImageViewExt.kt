package com.leventgorgu.cryptoinfo.util

import android.content.Context
import android.content.SharedPreferences
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.leventgorgu.cryptoinfo.R
import kotlin.math.abs

fun ImageView.loadImageWithGlide(imageUrl:String,circularProgressDrawable: CircularProgressDrawable){
    val options = RequestOptions()
        .placeholder(circularProgressDrawable)
        .error(R.mipmap.ic_launcher_round)

    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(imageUrl)
        .into(this)
}

fun placeHolderProgressBar(context: Context):CircularProgressDrawable{
    return CircularProgressDrawable(context).apply {
        strokeWidth = 8f
        centerRadius = 40f
        start()
    }
}

@BindingAdapter("android:loadImageUrl")
fun loadImage(imageView: ImageView,cryptoId:String?) {
    cryptoId?.let {
        val imageUrl = "https://s2.coinmarketcap.com/static/img/coins/128x128/$cryptoId.png"
        imageView.loadImageWithGlide(imageUrl,placeHolderProgressBar(imageView.context))
    }
}

/** Formats a 24h change as "+1.15%" / "−0.86%" and colors it green (up) or red (down). */
@BindingAdapter("changePercent")
fun bindChangePercent(textView: TextView, change: Double) {
    val up = change >= 0
    val sign = if (up) "+" else "−"
    textView.text = String.format("%s%.2f%%", sign, abs(change))
    val colorRes = if (up) R.color.up else R.color.down
    textView.setTextColor(ContextCompat.getColor(textView.context, colorRes))
}