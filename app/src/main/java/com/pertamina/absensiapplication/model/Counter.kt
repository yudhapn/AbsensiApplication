package com.pertamina.absensiapplication.model

import com.google.gson.annotations.SerializedName

data class Counter(
        @SerializedName("counterConfirm")
        val counterConfirm: Int = 0,
        @SerializedName("counterComplete")
        val counterComplete: Int = 0,
        @SerializedName("counterRequest")
        val counterRequest: Int = 0
)