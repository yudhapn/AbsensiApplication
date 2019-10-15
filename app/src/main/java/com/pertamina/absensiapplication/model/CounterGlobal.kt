package com.pertamina.absensiapplication.model

import com.google.gson.annotations.SerializedName

data class CounterGlobal(
    @SerializedName("counterApproved")
    val counterApproved: Int = 0,
    @SerializedName("counterOrganic")
    val counterOrganic: Int = 0,
    @SerializedName("counterTkjp")
    val counterTkjp: Int = 0
)