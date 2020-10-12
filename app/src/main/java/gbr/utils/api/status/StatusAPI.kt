package gbr.utils.api.status

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface StatusAPI : TextMessageWatcher, DestroyableAPI {
    var onStatusListener: OnStatusListener?
    fun sendStatusRequest(status: String, complete: (Boolean) -> Unit)
}