package newVersion.network.status

import ru.rubeg38.rubegprotocol.TextMessageWatcher
import javax.security.auth.Destroyable

interface StatusApi:TextMessageWatcher,newVersion.commonInterface.Destroyable {
    var OnStatusListener:OnStatusListener?
    fun sendStatusRequest(status:String,complete:(Boolean)->Unit)
}