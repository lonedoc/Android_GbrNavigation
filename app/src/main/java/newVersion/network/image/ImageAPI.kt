package newVersion.network.image

import ru.rubeg38.rubegprotocol.BinaryMessageWatcher
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import javax.security.auth.Destroyable

interface ImageAPI:TextMessageWatcher,BinaryMessageWatcher,newVersion.commonInterface.Destroyable {
    var onImageListener:OnImageListener?
    fun sendImageRequest(image:String,complete:(Boolean)->Unit)
    fun sendReceiveRequest(complete: (Boolean) -> Unit)
}