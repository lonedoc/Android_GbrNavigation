package gbr.utils.api.auth

import gbr.utils.data.AuthInfo

interface OnAuthListener {
    fun onAuthDataReceived(auth: AuthInfo)
}