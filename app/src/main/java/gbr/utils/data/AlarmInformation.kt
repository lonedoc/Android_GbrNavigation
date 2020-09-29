package gbr.utils.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AlarmInformation (
    var command: String? = null,
    var name: String? = null,
    var phone:String? = null,
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

data class AreaInfo(val name: String, val alarmtime: String) : Serializable

data class ResponsibleList(val name: String, val position: String, val phone: String, val phoneh: String, val phonew: String, val address: String) : Serializable