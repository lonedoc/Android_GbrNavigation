package ru.rubeg38.rubegprotocol

class Headers {
    var contentType: ContentType
    var messageNumber: Long
    var messageSize: Int
    var packetsCount: Int
    var packetNumber: Int
    var packetSize: Int
    var shift: Int
    var firstSize: Int
    var secondSize: Int
    var sessionId: String?

    constructor(
        contentType: ContentType,
        messageNumber: Long,
        messageSize: Int,
        packetsCount: Int,
        packetNumber: Int,
        packetSize: Int,
        shift: Int,
        firstSize: Int,
        secondSize: Int,
        sessionId: String?
    ) {
        this.contentType = contentType
        this.messageNumber = messageNumber
        this.messageSize = messageSize
        this.packetsCount = packetsCount
        this.packetNumber = packetNumber
        this.packetSize = packetSize
        this.shift = shift
        this.firstSize = firstSize
        this.secondSize = secondSize
        this.sessionId = sessionId
    }
}
