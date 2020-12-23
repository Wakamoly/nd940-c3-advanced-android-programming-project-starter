package com.udacity.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationModel(
    val file_name: String,
    val status: String
) : Parcelable
