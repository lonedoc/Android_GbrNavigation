package oldVersion.commonactivity

import android.util.Log
import java.io.Serializable
import newVersion.Utils.ResponsibleList

class AlarmObjectInfo() : Serializable {
    var name: String? = null
    var number: String? = null
    var lon: Double? = null
    var lat: Double? = null
    var inn: String? = null
    var zakaz: String? = null
    var address: String? = null
    var areaName: String? = null
    var areaAlarmTime: String? = null
    val planAndPhotoList: ArrayList<String> = ArrayList()
    val responsibleList: ArrayList<ResponsibleList> = ArrayList()
    val reportsList: ArrayList<String> = ArrayList()
    constructor(
        name: String,
        number: String,
        lon: Double?,
        lat: Double?,
        inn: String?,
        zakaz: String,
        address: String,
        areaName: String,
        areaAlarmTime: String,
        planAndPhotoList: ArrayList<String> = ArrayList(),
        responsibleList: ArrayList<ResponsibleList> = ArrayList(),
        reportsList: ArrayList<String> = ArrayList()
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
        this.responsibleList.addAll(responsibleList)
        this.reportsList.addAll(reportsList)
    }

    fun isNotEmpty(): Boolean {
        return this.name != null || this.number != null || this.lon != null || this.lat != null || this.inn != null || this.zakaz != null || this.address != null || this.areaName != null || this.areaAlarmTime != null || this.planAndPhotoList.count()> 0 || this.responsibleList.count()> 0 || this.reportsList.count()> 0
    }
    fun clear() {
        this.name = null
        this.number = null
        this.lon = null
        this.lat = null
        this.inn = null
        this.zakaz = null
        this.address = null
        this.areaName = null
        this.areaAlarmTime = null
        this.planAndPhotoList.clear()
        this.responsibleList.clear()
        this.reportsList.clear()
    }
    fun print() {
        Log.d(
            "AlarmObjectInfo",
            " \n \n Name = $name \n Number = $number \n Lon = $lon \n Lat = $lat \n Zakaz = $zakaz \n address = $address \n areaName = $areaName \n " +
                "areaAlarmTime = $areaAlarmTime \n $planAndPhotoList \n $responsibleList \n $reportsList"
        )
    }
}