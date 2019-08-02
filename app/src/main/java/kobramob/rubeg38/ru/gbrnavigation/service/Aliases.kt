package kobramob.rubeg38.ru.gbrnavigation.service

typealias ResponseHandler = (Boolean, ByteArray?) -> Unit
typealias ResultHandler = (Boolean) -> Unit

class RetransmissionInfo(
    var packet: Packet,
    var lastAttemptTime: Long,
    var attemptsCount: Int
)
