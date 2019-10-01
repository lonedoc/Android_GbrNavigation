package newVersion.network.auth

import newVersion.models.Auth

interface OnAuthListener {
    fun onAuthDataReceived(auth: Auth)
}