package com.example.ux.model

import java.io.Serializable

data class User(
    var uid: String? = "",
    var username: String? = "",
    var university: String? = "",
    var major: String? = "",
    var profileColor: String? = ""
) : Serializable {
}
