package app.shopgeo.`in`.user.data.model

data class LoginCredential(
    val username : String,
    val password : String,
    val device_id : String,
    val name : String
)
