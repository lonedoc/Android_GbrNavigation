package newVersion.network.image

import com.google.gson.JsonObject
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.BinaryMessageWatcher
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPImageAPI (
    private val protocol:RubegProtocol
):ImageAPI{
    override var onImageListener:OnImageListener? = null
    private var unsubscribeText = protocol.subscribe(this as TextMessageWatcher)
    private var unsubscribeBinary = protocol.subscribe(this as BinaryMessageWatcher)

    override fun sendImageRequest(image: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "getfile")
        jsonObject.addProperty("nameinserv",image)
        jsonObject.addProperty("name",image)

        val request = jsonObject.toString()
        protocol.send(request,complete)
    }

    override fun sendReceiveRequest(complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$","startrecivefile")

        val request = jsonObject.toString()
        protocol.send(request,complete)
    }

    override fun onTextMessageReceived(message: String) {

        if(JSONObject(message).getString("\$c$") != "sendfile") return

        sendReceiveRequest { }
    }

    override fun onBinaryMessageReceived(message: ByteArray) {
        onImageListener?.onImageDataReceived(message)
    }

    override fun onDestroy() {
        unsubscribeBinary()
        unsubscribeText()
    }
}