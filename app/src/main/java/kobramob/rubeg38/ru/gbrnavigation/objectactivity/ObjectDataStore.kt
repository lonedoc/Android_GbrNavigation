package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.graphics.Bitmap

object ObjectDataStore {

    var timeToArrived:String? = null
    var timeAlarmApplyLong:Long? = null
    var timeAlarmApply:String? = null
    var bitmapList:ArrayList<Bitmap> = ArrayList()
    var arrivedToObjectSend:Boolean = false
    var putOffArrivedToObjectSend:Boolean = false

    fun saveToTimeToArrived(time:Int){
        val hours = time/3600
        val minute = (time%3600)/60
        val seconds = time%60
        timeToArrived = "Время в пути: $hours:$minute:$seconds"
    }

    fun timeAlarmApply(time:String){
        this.timeAlarmApply = time
    }

    fun clearAllAlarmData()
    {
        if(bitmapList.count()>1){
            bitmapList.clear()
        }
        PlanFragment.countInQueue = 0
        this.timeToArrived = null
        this.timeAlarmApply = null
        arrivedToObjectSend = false
        putOffArrivedToObjectSend = false
    }



}