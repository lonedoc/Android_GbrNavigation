package gbr.utils.api.serverstatus

import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPServerStatusAPI(
    protocol: RubegProtocol
):ServerStatusAPI {

    override var onServerStatusListener: OnServerStatusListener? = null
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun onTextMessageReceived(message: String) {

        try{
            if (!JSONObject(message).has("\$c$")) {
                return
            }
        }catch (e:Exception){
            return
        }

        if(JSONObject(message).getString("\$c$") != "ServerNotResponse") return
        onServerStatusListener?.onServerStatusDataReceived()
        return
    }

    override fun onDestroy() {
        unsubscribe
    }

}