package kobramob.rubeg38.ru.networkprotocol

typealias ResultHandler = (Boolean) -> Unit
typealias ResponseHandler = (Boolean, ByteArray?) -> Unit

class RetransmissionInfo(
    var packet: Packet,
    var lastAttemptTime: Long,
    var attemptsCount: Int
)