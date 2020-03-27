package newVersion.models

import gbr.utils.data.CityCard
import gbr.utils.data.StatusList


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
    var gpsStatus: ArrayList<StatusList>,
    var routeServer: ArrayList<String>,
    var report: ArrayList<String>,
    var cityCard: CityCard,
    val lastVersion:String?
)