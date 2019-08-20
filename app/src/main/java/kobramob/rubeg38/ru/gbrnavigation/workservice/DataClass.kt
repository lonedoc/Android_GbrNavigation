package kobramob.rubeg38.ru.gbrnavigation.workservice

import java.io.Serializable

class DataClass
data class AlarmGson(
    val command: String,
    val name: String,
    val number: String,
    var lon: String=" ",
    var lat: String=" ",
    var inn: String=" ",
    var zakaz: String? = null,
    val address: String,
    val area: AreaInfo,
    val otvl: ArrayList<OtvlList> = ArrayList(),
    val plan: ArrayList<String> = ArrayList(),
    val photo: ArrayList<String> = ArrayList()
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

data class RegistrationGson(
    val command: String,
    val tid: String,
    val call: String,
    val status: String,
    val gpsstatus: ArrayList<String> = ArrayList(),
    val routeserver: ArrayList<String> = ArrayList(),
    val reports: ArrayList<String> = ArrayList(),
    val namegbr:String
)

data class AreaInfo(val name: String, val alarmtime: String)

data class OtvlList(val name: String, val position: String, val phone: String, val phoneh: String, val phonew: String, val address: String) : Serializable
