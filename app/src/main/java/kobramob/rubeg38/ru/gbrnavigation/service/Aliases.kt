package kobramob.rubeg38.ru.gbrnavigation.service

import java.nio.ByteBuffer
import java.sql.Time

typealias ResultHandler = (Boolean, ByteArray) -> Unit

typealias RetransmissionInfo = (
    packet: Packet,
    timeToTransmit: Time,
    attemptsCounts: UInt
) -> Unit

typealias TransmissionInfo = (
    acknowledgements: BooleanArray,
    isWaitingForResponse: Boolean,
    resultHandler: ResultHandler
) -> Unit

typealias ReceivingInfo = (
    packet: BooleanArray?,
    data: ByteBuffer?,
    time: Time,
    resultHandler: ResultHandler
) -> Unit