package gbr.utils.api.alarm

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface AlarmAPI:TextMessageWatcher, DestroyableAPI {
    var onAlarmListener:OnAlarmListener?
    fun sendAlarmRequest(namegbr:String,complete:(Boolean)->Unit)
    fun sendAlarmApplyRequest(
        objectNumber:String,
        lat:Double,
        lon:Double,
        speed: Float,
        complete:(Boolean)->Unit)
    fun sendArrivedObject(
        objectNumber: String,
        lat:Double,
        lon:Double,
        speed: Float, complete: (Boolean) -> Unit)
    fun sendMobAlarmApplyRequest(
        objectNumber:String,
        lat:Double,
        lon:Double,
        speed: Float,
        complete:(Boolean)->Unit)
    fun sendMobArrivedObject(
        objectNumber: String,
        lat:Double,
        lon:Double,
        speed: Float, complete: (Boolean) -> Unit)
    fun sendReport(report: String, comment: String, namegbr: String, objectName: String, objectNumber: String, complete: (Boolean) -> Unit)
}