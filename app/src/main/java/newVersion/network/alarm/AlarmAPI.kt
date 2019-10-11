package newVersion.network.alarm

import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface AlarmAPI : TextMessageWatcher, newVersion.commonInterface.Destroyable {
    var onAlarmListener: OnAlarmListener?
    fun sendAlarmRequest(namegbr: String, complete: (Boolean) -> Unit)
    fun sendAlarmApplyRequest(objectNumber: String, complete: (Boolean) -> Unit)
}