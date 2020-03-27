package newVersion.network.auth

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface AuthAPI : TextMessageWatcher, DestroyableAPI {
    var onAuthListener: OnAuthListener?
    fun sendAuthRequest(complete: (Boolean) -> Unit)
}