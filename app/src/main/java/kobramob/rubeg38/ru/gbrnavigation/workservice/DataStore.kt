package kobramob.rubeg38.ru.gbrnavigation.workservice

object DataStore {
    var reports:ArrayList<String> = ArrayList()
    var namegbr:String = "гбр"
    var call:String = ""
    var status:String = ""
    var routeServer:ArrayList<String> = ArrayList()
    var statusList:ArrayList<String> = ArrayList()

    fun initRegistrationData(namegbr:String,call:String,status:String,statusList:ArrayList<String>,routeServer:ArrayList<String>,reports:ArrayList<String>){

        this.namegbr = namegbr

        this.call = call

        this.status = status

        this.statusList.addAll(statusList)

        if(routeServer.count()>1)
            this.routeServer.addAll(routeServer)
        else
            this.routeServer.add("91.189.160.38:5000")

        this.reports = reports
    }

}