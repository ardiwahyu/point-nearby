package com.gitlab.pointnearby.utils

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter

object BindAdapters {
    @SuppressLint("SetTextI18n")
    @BindingAdapter("setLatitude")
    @JvmStatic
    fun setLatitude(textView: TextView, double: Double){
        textView.text = "Lat: $double"
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter("setLongitude")
    @JvmStatic
    fun setLongitude(textView: TextView, double: Double){
        textView.text = "Long: $double"
    }
}