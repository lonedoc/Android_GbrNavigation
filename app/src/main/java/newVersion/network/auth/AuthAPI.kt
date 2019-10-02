package newVersion.network.auth

import newVersion.commonInterface.Destroyable
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface AuthAPI : TextMessageWatcher, Destroyable {
    var onAuthListener: OnAuthListener?
    fun sendAuthRequest(complete: (Boolean) -> Unit)
}