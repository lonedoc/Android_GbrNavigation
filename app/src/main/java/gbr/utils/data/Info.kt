package gbr.utils.data

import android.content.Context
import java.util.ArrayList

object Info {
    var call:String? = null
    var status:String? = null
    var nameGBR:String? = null
    var dist:Int?=null
    var statusList:ArrayList<StatusList>? = null
    var routeServers:ArrayList<String>? = null
    var reportsList: ArrayList<String>? = null

    fun call(call:String)
    {
        this.call = call
    }
    fun dist(dist:Int){
        this.dist = dist
    }
    fun status(status:String)
    {
        this.status = status
    }

    fun statusList(statusList: ArrayList<StatusList>) {
        this.statusList = statusList
    }

    fun nameGBR(nameGBR:String){
        this.nameGBR = nameGBR
    }

    fun clearAllData() {

    }

    fun routeServers(routeServerList: ArrayList<String>) {
        this.routeServers = routeServerList
    }

    fun reportList(reportList:ArrayList<String>){
        this.reportsList = reportList
    }


}