package ru.rubeg38.rubegprotocol

typealias ResultHandler = (Boolean) -> Unit
typealias ResponseHandler = (Boolean, ByteArray?) -> Unit

class RetransmissionInfo(
        var packet: Packet,
        var lastAttemptTime: Long,
        var attemptsCount: Int
)

class Transmission(
    var packet: Packet,
    var lastAttemptTime: Long,
    var attemptsCount: Int
)