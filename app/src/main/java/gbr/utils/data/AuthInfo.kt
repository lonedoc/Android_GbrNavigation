package gbr.utils.data

import com.google.gson.annotations.SerializedName

data class AuthInfo(
    val command: String,
    @SerializedName("tid") val token: String,
    val call: String,
    val status: String,
    @SerializedName("gpsstatus") val statusList: ArrayList<StatusList> = ArrayList(),
    @SerializedName("routeserver") val routeServerList: ArrayList<String> = ArrayList(),
    @SerializedName("reports") val reportsList: ArrayList<String> = ArrayList(),
    val namegbr: String,
    @SerializedName("citycard")val cityCard: CityCard,
    val autoarrival:String
)