package com.mdp.caremate.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    @SerializedName("id")
    val eid: String = "",

    @SerializedName("title")
    val name: String = "",

    val date: String = "",
    val time: String = "",

    @SerializedName("location")
    val place: String = "",

    @SerializedName("slots")
    val capacity: String = "",

    val photoUrl: String = "",

    val listed: Boolean = false
) : Parcelable