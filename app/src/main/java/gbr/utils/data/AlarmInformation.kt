package gbr.utils.data

import com.google.gson.annotations.SerializedName
import newVersion.utils.AreaInfo
import newVersion.utils.ResponsibleList
import java.io.Serializable

data class AlarmInformation (
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