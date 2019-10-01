package oldVersion.resource

import oldVersion.workservice.CityCard
import oldVersion.workservice.GpsStatus

object DataStore {

    var reports:ArrayList<String> = ArrayList()

    var namegbr:String = "гбр"

    var call:String = ""

    var status:String = ""

    var routeServer:ArrayList<String> = ArrayList()

    var statusList:ArrayList<GpsStatus> = ArrayList()

    lateinit var cityCard: CityCard

    fun initRegistrationData(namegbr:String, call:String, status:String, statusList:ArrayList<GpsStatus>, routeServer:ArrayList<String>, reports:ArrayList<String>, cityCard: CityCard)
    {
        DataStore.namegbr = namegbr

        DataStore.call = call

        DataStore.status = status

        DataStore.statusList.addAll(statusList)
        statusList.sortBy { it.name }

        if(routeServer.count()>0)
            DataStore.routeServer.addAll(routeServer)
        else
            DataStore.routeServer.add("91.189.160.38:5000")


        DataStore.reports = reports

        DataStore.cityCard = cityCard
    }

    fun clearAllData()
    {
        if(reports.count()>0)
        reports.clear()

        if(routeServer.count()>0)
        routeServer.clear()

        if(statusList.count()>0)
        statusList.clear()

        namegbr = ""
        call = ""
        status =""
    }
}