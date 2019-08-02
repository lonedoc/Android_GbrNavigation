package kobramob.rubeg38.ru.gbrnavigation.service

class Headers(
    var contentType: ContentType,
    var messageNumber: Long,
    var messageSize: Int,
    var packetsCount: Int,
    var packetNumber: Int,
    var packetSize: Int,
    var shift: Int,
    var firstSize: Int,
    var secondSize: Int,
    var sessionId: String?
)