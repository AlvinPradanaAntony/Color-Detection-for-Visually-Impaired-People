package com.devcode.colordetection

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListRes(
    val resolution: String,
    val width: Int,
    val height: Int
) : Parcelable
