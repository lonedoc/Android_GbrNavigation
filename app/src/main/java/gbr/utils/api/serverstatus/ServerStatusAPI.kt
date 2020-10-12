package gbr.utils.api.serverstatus

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface ServerStatusAPI:TextMessageWatcher,
    DestroyableAPI {
    var onServerStatusListener:OnServerStatusListener?
}