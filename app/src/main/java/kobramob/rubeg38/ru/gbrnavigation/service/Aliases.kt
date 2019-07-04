package kobramob.rubeg38.ru.gbrnavigation.service

import java.sql.Time

typealias ResponseHandler = (Boolean, ByteArray?) -> Unit
typealias ResultHandler = (Boolean) -> Unit

typealias RetransmissionInfo = (
    packet: Packet,
    timeToTransmit: Time,
    attemptsCounts: UInt
) -> Unit
