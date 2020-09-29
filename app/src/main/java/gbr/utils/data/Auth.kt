package gbr.utils.data


data class Auth(
    var authInfo: AuthInfo?,
    var authorized: Boolean,
    var accessDenied: Boolean
)
