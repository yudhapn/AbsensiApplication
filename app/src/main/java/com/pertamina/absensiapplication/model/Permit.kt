package com.pertamina.absensiapplication.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class Permit(
        @SerializedName("permitId")
        var permitId: String = "",
        @SerializedName("permitNumber")
        var permitNumber: String = "",
        @SerializedName("title")
        val title: String = "",
        @SerializedName("employeeNumber")
        val employeeNumber: String = "",
        @SerializedName("from")
        val from: String = "",
        @SerializedName("to")
        val to: String = "",
        @SerializedName("leaveDuration")
        var leaveDuration: Int = 0,
        @SerializedName("dateTo")
        var dateTo: String = "",
        @SerializedName("dateBack")
        var dateBack: String = "",
        @SerializedName("dateIn")
        var dateIn: String = "",
        @SerializedName("cost")
        val cost: String = "",
        @SerializedName("drive")
        val drive: String = "",
        @SerializedName("status")
        var status: StatusPermit = StatusPermit(),
        @SerializedName("userId")
        val userId: String = "",
        @SerializedName("senior")
        var senior: String = "",
        @SerializedName("operationHead")
        var operationHead: String = "",
        @SerializedName("profileImage")
        var profileImage: String = "",
        @SerializedName("applicantName")
        val applicantName: String = "",
        @SerializedName("type")
        val type: List<String> = ArrayList(),
        @SerializedName("createdOn")
        @ServerTimestamp
        val createdOn: Date = Calendar.getInstance().time
) : Parcelable