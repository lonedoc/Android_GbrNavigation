package kobramob.rubeg38.ru.gbrnavigation.commonactivity

import android.util.Log
import java.io.Serializable
import kobramob.rubeg38.ru.gbrnavigation.workservice.OtvlList

class AlarmObjectInfo() : Serializable {
    var name: String = ""
    var number: String = ""
    var lon: Double? = null
    var lat: Double? = null
    var inn: Long? = null
    var zakaz: String = ""
    var address: String = ""
    var areaName: String = ""
    var areaAlarmTime: String = ""
    val planAndPhotoList: ArrayList<String> = ArrayList()
    val otvlList: ArrayList<OtvlList> = ArrayList()
    constructor(
        name: String,
        number: String,
        lon: Double?,
        lat: Double?,
        inn: Long?,
        zakaz: String,
        address: String,
        areaName: String,
        areaAlarmTime: String,
        planAndPhotoList: ArrayList<String> = ArrayList(),
        otvlList: ArrayList<OtvlList> = ArrayList()
    ) : this() {
        this.name = name
        this.number = number
        this.lon = lon
        this.lat = lat
        this.inn = inn
        this.zakaz = zakaz
        this.address = address
        this.areaName = areaName
        this.areaAlarmTime = areaAlarmTime
        this.planAndPhotoList.addAll(planAndPhotoList)
        this.otvlList.addAll(otvlList)
    }

    fun clear() {
        this.name = ""
        this.number = ""
        this.lon = 0.0
        this.lat = 0.0
        this.inn = 0
        this.zakaz = ""
        this.address = ""
        this.areaName = ""
        this.areaAlarmTime = ""
        this.planAndPhotoList.clear()
        this.otvlList.clear()
    }
    fun print() {
        Log.d(
            "AlarmObjectInfo",
            " \n \n Name = $name \n Number = $number \n Lon = $lon \n Lat = $lat \n Zakaz = $zakaz \n address = $address \n areaName = $areaName \n " +
                "areaAlarmTime = $areaAlarmTime \n $planAndPhotoList \n $otvlList"
        )
    }
}