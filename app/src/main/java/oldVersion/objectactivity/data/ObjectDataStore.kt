package oldVersion.objectactivity.data

import android.graphics.Bitmap
import oldVersion.objectactivity.planfragment.PlanFragment

object ObjectDataStore {

    var timeToArrived: String? = null
    var timeAlarmApplyLong: Long? = null
    var timeAlarmApply: String? = null
    var bitmapList: ArrayList<Bitmap> = ArrayList()
    var arrivedToObjectSend: Boolean = false
    var putOffArrivedToObjectSend: Boolean = false

    fun saveToTimeToArrived(time: Int) {
        val hours = time / 3600
        val minute = (time % 3600) / 60
        val seconds = time % 60
        timeToArrived = "Время в пути: $hours:$minute:$seconds"
    }

    fun timeAlarmApply(time: String) {
        timeAlarmApply = time
    }

    fun clearAllAlarmData() {
        if (bitmapList.count()> 1) {
            bitmapList.clear()
        }
        PlanFragment.countInQueue = 0
        timeToArrived = null
        timeAlarmApply = null
        arrivedToObjectSend = false
        putOffArrivedToObjectSend = false
    }
}