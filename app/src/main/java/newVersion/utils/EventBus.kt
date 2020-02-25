package newVersion.utils

import newVersion.models.Credentials

data class newCredetials(
    val credetials: Credentials
)

data class providerStatus(
    val status:String
)
data class location(
    val lat:Double,
    val lon:Double,
    val accuracy:Float,
    val speed:Float
)