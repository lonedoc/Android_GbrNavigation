package ru.rubeg38.rubegprotocol

interface Packet {
    val headers: Headers
    val data: ByteArray?
    fun encode(): ByteArray
}

class DataPacket: Packet {
    override val headers: Headers
    override val data: ByteArray?

    constructor(
        data: ByteArray,
        sessionId: String?,
        contentType: ContentType,
        messageNumber: Long,
        messageSize: Int,
        shift: Int,
        packetsCount: Int,
        packetNumber: Int
    ) {
        this.data = data

        val packetSize = data.count() + 2

        this.headers = Headers(
            contentType,
            messageNumber,
            messageSize,
            packetsCount,
            packetNumber,
            packetSize,
            shift,
            0,
            0,
            sessionId
        )
    }

    constructor(data: ByteArray, headers: Headers) {
        this.data = data
        this.headers = headers
    }

    override fun encode(): ByteArray {
        val coder = Coder()

        return coder.encode(this.data!!, this.headers)
    }
}

class AcknowledgementPacket: Packet {
    override val headers: Headers
    override val data: ByteArray?

    constructor(packet: DataPacket) {
        this.data = null

        this.headers = Headers(
            ContentType.ACKNOWLEDGEMENT,
            packet.headers.messageNumber,
            packet.headers.messageSize,
            packet.headers.packetsCount,
            packet.headers.packetNumber,
            packet.headers.packetSize,
            packet.headers.shift,
            packet.headers.firstSize,
            packet.headers.secondSize,
            packet.headers.sessionId
        )
    }

    constructor(headers: Headers) {
        this.data = null
        this.headers = headers

        // ?
        this.headers.contentType = ContentType.ACKNOWLEDGEMENT
    }

    override fun encode(): ByteArray {
        val coder = Coder()

        return coder.encode(headers = this.headers)
    }
}

class ConnectionPacket: Packet {
    override val headers: Headers
    override val data: ByteArray?

    constructor(sessionId: String) {
        this.data = null

        this.headers = Headers(
            ContentType.CONNECTION,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            sessionId
        )
    }

    constructor(headers: Headers) {
        this.data = null
        this.headers = headers
    }

    override fun encode(): ByteArray {
        val coder = Coder()

        return coder.encode(headers = this.headers)
    }

}

class PacketUtils {
    companion object {
        fun decode(data: ByteArray): Packet? {
            val coder = Coder()

            val (headers, body) = coder.decode(data)

            if(headers==null && body == null)
                return null

            if (headers?.contentType == ContentType.ACKNOWLEDGEMENT) {
                return AcknowledgementPacket(headers)
            }

            if (headers?.contentType == ContentType.CONNECTION) {
                return ConnectionPacket(headers)
            }

            return DataPacket(body!!, headers!!)
        }
    }
}
