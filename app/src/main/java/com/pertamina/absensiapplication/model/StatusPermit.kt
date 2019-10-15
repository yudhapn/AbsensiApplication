package com.pertamina.absensiapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StatusPermit(
        @SerializedName("negotiate")
        var negotiate: Boolean = false,
        @SerializedName("confirmBySenior")
        var confirmBySenior: Boolean = false,
        @SerializedName("confirmByOH")
        var confirmByOH: Boolean = false,
        @SerializedName("complete")
        var complete: Boolean = false,
        @SerializedName("request")
        var request: Boolean = false,
        @SerializedName("cancel")
        var cancel: Boolean = false,
        @SerializedName("manual")
        var manual: Boolean = false
) : Parcelable