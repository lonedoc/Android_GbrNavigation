package kobramob.rubeg38.ru.gbrnavigation.workservice

import android.util.Log

object DataStore {

    var appClosed = false

    var reports:ArrayList<String> = ArrayList()

    var namegbr:String = "гбр"

    var call:String = ""

    var status:String = ""

    var routeServer:ArrayList<String> = ArrayList()

    var statusList:ArrayList<GpsStatus> = ArrayList()

    lateinit var cityCard:CityCard

    fun initRegistrationData(namegbr:String,call:String,status:String,statusList:ArrayList<GpsStatus>,routeServer:ArrayList<String>,reports:ArrayList<String>,cityCard: CityCard)
    {
        this.namegbr = namegbr

        this.call = call

        this.status = status

        this.statusList.addAll(statusList)
        statusList.sortBy { it.name }

        if(routeServer.count()>0)
            this.routeServer.addAll(routeServer)
        else
            this.routeServer.add("91.189.160.38:5000")


        this.reports = reports

        this.cityCard = cityCard
    }

    fun clearAllData()
    {
        if(this.reports.count()>0)
        this.reports.clear()

        if(this.routeServer.count()>0)
        this.routeServer.clear()

        if(this.statusList.count()>0)
        this.statusList.clear()

        this.namegbr = ""
        this.call = ""
        this.status =""
    }
}