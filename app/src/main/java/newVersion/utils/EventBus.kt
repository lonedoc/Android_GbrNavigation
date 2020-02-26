package newVersion.utils

import newVersion.models.Credentials

data class NewCredentials(
    val credentials: Credentials
)

data class ProviderStatus(
    val status:String
)
data class Location(
    val lat:Double,
    val lon:Double,
    val accuracy:Float,
    val speed:Float,
    val satelliteCount:Int?
)
data class LocationInfo(
    val lat:Double,
    val lon:Double,
    val accuracy:Float,
    val speed:Int,
    val satelliteCount:Int?
)