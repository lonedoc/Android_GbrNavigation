package rubeg38.myalarmbutton.utils.api.coordinate

import gbr.utils.interfaces.DestroyableAPI
import ru.rubeg38.rubegprotocol.TextMessageWatcher

interface CoordinateAPI:TextMessageWatcher, DestroyableAPI {
    var onCoordinateListener:OnCoordinateListener?
    fun sendCoordinateRequest(
        lat: String,
        lon: String,
        imei: String,
        speed: Int,
        satelliteCount: Int,
        accuracy: Float
    )
}