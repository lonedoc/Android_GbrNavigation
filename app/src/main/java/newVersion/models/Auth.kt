package newVersion.models

import newVersion.utils.CityCard
import newVersion.utils.GpsStatus

data class Auth(
    var authInfo: AuthInfo?,
    var authorized: Boolean,
    var accessDenied: Boolean
)

data class AuthInfo(
    var token: String?,
    var nameGbr: String?,
    var call: String?,
    var status: String?,
    var gpsStatus: ArrayList<GpsStatus>,
    var routeServer: ArrayList<String>,
    var report: ArrayList<String>,
    var cityCard: CityCard
)