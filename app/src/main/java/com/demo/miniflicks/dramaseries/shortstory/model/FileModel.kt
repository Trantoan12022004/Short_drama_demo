package com.demo.miniflicks.dramaseries.shortstory.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LanguageModelOscar(
    var name: String, var code: String, var image: Int, var active: Boolean,
) : Parcelable

@Parcelize
data class IntroModelOscar(
    var title: Int = -1, var content: Int = -1, var image: Int = 0,
) : Parcelable