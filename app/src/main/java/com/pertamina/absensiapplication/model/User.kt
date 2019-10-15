package com.pertamina.absensiapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    @SerializedName("userId")
    var userId: String = "",
    @SerializedName("employeeNumber")
    var employeeNumber: String = "",
    @SerializedName("leaveBalance")
    var leaveBalance: Int = 0,
    @SerializedName("name")
    var name: String = "",
    @SerializedName("position")
    var position: String = "",
    @SerializedName("senior")
    var senior: String = "",
    @SerializedName("operationHead")
    var operationHead: String = "",
    @SerializedName("profileImage")
    var profileImage: String = "",
    @SerializedName("organic")
    var organic: Boolean = false,
    @SerializedName("leave")
    var leave: Boolean = false,
    @SerializedName("token")
    var token: String = "",
    @SerializedName("pjs")
    var pjs: String = "",
    @SerializedName("standIn")
    var standIn: MutableList<String> = mutableListOf(),
    @SerializedName("operator")
    var operator: Boolean = false
    ) : Parcelable