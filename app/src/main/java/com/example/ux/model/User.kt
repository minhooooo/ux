package com.example.ux.model

import java.io.Serializable

data class User(
    var uid: String? = "",
    var username: String? = "",
    var email: String? = "",
    var profileImageUrl: String? = "",
    var status: String? = "",
) : Serializable {
}
