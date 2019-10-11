package newVersion.network.status

import javax.security.auth.Destroyable
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface StatusAPI : TextMessageWatcher, newVersion.commonInterface.Destroyable {
    var onStatusListener: OnStatusListener?
    fun sendStatusRequest(status: String, complete: (Boolean) -> Unit)
}