package gbr.utils.api.access

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface AccessAPI:TextMessageWatcher, DestroyableAPI {
    var onAccessListener:OnAccessListener?
}