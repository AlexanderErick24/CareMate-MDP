package com.mdp.caremate.data.model

data class Event(
    val eid: String = "",
    val name: String = "",
    val date: String = "",
    val time: String = "",
    val place: String = "",
    val capacity: String = "",
    val listed: Boolean = false
)
