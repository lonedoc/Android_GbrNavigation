package newVersion.network.alarm

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface AlarmAPI : TextMessageWatcher, DestroyableAPI {
    var onAlarmListener: OnAlarmListener?
    fun sendAlarmRequest(namegbr: String, complete: (Boolean) -> Unit)
    fun sendAlarmApplyRequest(objectNumber: String, complete: (Boolean) -> Unit)
}