package newVersion.network.status

interface OnStatusListener {
    fun onStatusDataReceived(status:String,call:String)
}