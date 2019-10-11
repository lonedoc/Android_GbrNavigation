package newVersion.network.complete

import newVersion.commonInterface.Destroyable
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface CompleteAPI : TextMessageWatcher, Destroyable {
    var onCompleteListener: OnCompleteListener?
    fun sendArrivedObject(objectNumber: String, complete: (Boolean) -> Unit)
    fun sendReport(report: String, comment: String, namegbr: String, objectName: String, objectNumber: String, complete: (Boolean) -> Unit)
}