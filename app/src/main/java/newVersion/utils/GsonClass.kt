package newVersion.utils

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Alarm(
    var command: String? = null,
    var name: String? = null,
    var number: String? = null,
    var lon: String? = null,
    var lat: String? = null,
    var inn: String? = null,
    var zakaz: String? = null,
    val address: String? = null,
    val area: AreaInfo? = null,
    @SerializedName("dop") var additionally:String? = null,
    @SerializedName("otvl") val responsibleList: ArrayList<ResponsibleList> = ArrayList(),
    val plan: ArrayList<String> = ArrayList(),
    val photo: ArrayList<String> = ArrayList()
) : Serializable

data class NotAlarmGson(
    val command: String,
    val name: String
)
data class StatusGson(
    val command: String,
    val number: String,
    val call: String,
    val status: String,
    val color: String,
    val member: ArrayList<String> = ArrayList(),
    val gpsstatus: ArrayList<String> = ArrayList()
)
data class AreaInfo(val name: String, val alarmtime: String) : Serializable

data class ResponsibleList(val name: String, val position: String, val phone: String, val phoneh: String, val phonew: String, val address: String) : Serializable
