package newVersion.network.complete

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface CompleteAPI : TextMessageWatcher,
    DestroyableAPI {
    var onCompleteListener: OnCompleteListener?
    fun sendArrivedObject(objectNumber: String, complete: (Boolean) -> Unit)
    fun sendReport(report: String, comment: String, namegbr: String, objectName: String, objectNumber: String, complete: (Boolean) -> Unit)
}