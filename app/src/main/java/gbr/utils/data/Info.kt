package gbr.utils.data

import java.util.ArrayList

object Info {
    var call:String? = null
    var status:String? = null
    var nameGBR:String? = null
    var statusList:ArrayList<StatusList>? = null

    fun call(call:String)
    {
        this.call = call
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


}