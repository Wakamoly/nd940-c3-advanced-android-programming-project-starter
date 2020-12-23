package com.udacity.models

data class NotiChannelDetails(
    val id: String,
    val name: String,
    val description: String,
    val importance: Int,
    val priority: Int,
    val visibility: Int
)
