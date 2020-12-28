package gbr.utils.data

import android.graphics.Bitmap

object AlarmInfo {
    var command: String? = null
    var name: String? = null
    var phone:String? = null
    var number: String? = null
    var lon: String? = null
    var lat: String? = null
    var inn: String? = null
    var zakaz: String? = null
    var address: String? = null
    var area: AreaInfo? = null
    var additionally:String? = null
    var responsibleList: ArrayList<ResponsibleList> = ArrayList()
    var plan: ArrayList<String> = ArrayList()
    var photo: ArrayList<String> = ArrayList()
    var downloadPhoto:ArrayList<Bitmap> = ArrayList()

    fun initAllData(information:AlarmInformation){
        this.name = information.name
        this.phone = information.phone
        this.number = information.number
        this.lon = information.lon
        this.lat = information.lat
        this.inn = information.inn
        this.zakaz = information.zakaz
        this.address = information.address
        this.area = information.area
        this.additionally = information.additionally
        this.responsibleList = information.responsibleList
        this.plan = information.plan
        this.photo = information.photo
    }

    fun clearData(){
        this.name = null
        this.phone = null
        this.number = null
        this.lon = null
        this.lat = null
        this.inn = null
        this.zakaz = null
        this.address = null
        this.area = null
        this.additionally = null
        this.responsibleList.clear()
        this.plan.clear()
        this.photo.clear()
        this.downloadPhoto.clear()
    }
}