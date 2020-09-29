package gbr.utils.data

interface Preferences {
    var serverAddress: ArrayList<String>
    var serverPort: Int
    var imei: String?
    var fcmtoken: String?
    var lastLunchTime: Long
    val containsAddress: Boolean
    val containsPort: Boolean
    val containsImei: Boolean
    val containsFcmToken: Boolean
    val containsLastLunchTime: Boolean
}