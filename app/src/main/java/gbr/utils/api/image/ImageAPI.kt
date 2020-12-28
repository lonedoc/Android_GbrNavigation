package gbr.utils.api.image

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.BinaryMessageWatcher
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface ImageAPI:TextMessageWatcher,BinaryMessageWatcher,
    DestroyableAPI {
    var onImageListener: OnImageListener?
    fun sendImageRequest(image:String,complete:(Boolean)->Unit)
    fun sendReceiveRequest(complete: (Boolean) -> Unit)
}