package kobramob.rubeg38.ru.gbrnavigation.workservice

import java.io.Serializable

class DataClass
data class AlarmGson(
    val command: String,
    val name: String,
    val number: String,
    val lon: String,
    val lat: String,
    val inn: String,
    val zakaz: String,
    val address: String,
    val area: AreaInfo,
    val otvl: ArrayList<OtvlList> = ArrayList(),
    val plan: ArrayList<String> = ArrayList(),
    val photo: ArrayList<String> = ArrayList()
)

data class RegistrationGson(
    val command: String,
    val tid: String,
    val call: String,
    val status: String,
    val gpsstatus: ArrayList<String> = ArrayList(),
    val routeserver: ArrayList<String> = ArrayList()
)

data class AreaInfo(val name: String, val alarmtime: String)

data class OtvlList(val name: String, val position: String, val phone: String, val phoneh: String, val phonew: String, val address: String) : Serializable
