package gbr.utils.api.status

interface OnStatusListener {
    fun onStatusDataReceived(status: String, call: String)
}