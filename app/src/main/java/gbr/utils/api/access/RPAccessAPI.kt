package gbr.utils.api.access

import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPAccessAPI(
    private val protocol: RubegProtocol
):AccessAPI {
    override var onAccessListener: OnAccessListener? = null

    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun onTextMessageReceived(message: String) {

        try{
            if (!JSONObject(message).has("\$c$")) {
                return
            }
        }catch (e:Exception){
            return
        }

        when (JSONObject(message).getString("\$c$")) {
            "accessdenied"->{
                onAccessListener?.onAccessDataReceiver(false)
                return
            }
            "regok"->{
                onAccessListener?.onAccessDataReceiver(true)
                return
            }
        }
    }

    override fun onDestroy() {
       unsubscribe()
    }

}