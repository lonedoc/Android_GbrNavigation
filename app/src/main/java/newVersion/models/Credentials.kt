package newVersion.models

import java.io.Serializable

data class Credentials(
    var imei: String,
    var fcmtoken: String
) : Serializable
