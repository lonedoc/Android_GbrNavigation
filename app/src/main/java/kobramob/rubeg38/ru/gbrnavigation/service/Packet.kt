package kobramob.rubeg38.ru.gbrnavigation.service

import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class PacketType {
    connection, acknowledgment, data
}

interface Packet {
    var type: PacketType?
    var headers: Headers?
    var data: ByteArray?
    fun encode(): DatagramPacket
 /*   fun encode(): ByteBuffer*/
}

class PacketUtils {

    companion object {
        fun decode(data: DatagramPacket): Packet {

            val coder = Coder()
            val dataArray: ByteArray = data.data

            val (headers, body) = coder.decoder(dataArray)

            if (headers.contentType == ContentType.acknowledgement) {
                return AcknowledgmentPacket(headers)
            }
            if (headers.contentType == ContentType.connection) {
                return ConnectionPacket(headers)
            }

            return DataPacket(body, headers)
        }
    }
}

class DataPacket() :
    Packet {
    override var type: PacketType? = null
    override var headers: Headers? = null
    override var data: ByteArray? = null

    constructor(data: ByteArray?, headers: Headers) : this() {
        this.data = data
        this.headers = headers
        this.type = PacketType.data
    }

    constructor(data: ByteArray, sessionID: String?, contentType: ContentType, messageNumber: Long, messageSize: Int, shift: Int, packetCount: Int, packetNumber: Int) : this() {
        this.type = PacketType.data
        this.data = data

        val packetSize = data.count() + 2
        this.headers = Headers(
            contentType,
            messageNumber,
            messageSize,
            packetCount,
            packetNumber,
            packetSize,
            shift,
            0,
            0,
            sessionID
        )
    }

    override fun encode(): DatagramPacket {
        this.type = PacketType.data

        val coder = Coder()
        val packet = coder.encoder(this.data!!, this.headers!!)
        return DatagramPacket(packet, packet.size)
    }
   /* override fun encode(): ByteBuffer {
        this.type = PacketType.data

        val coder = Coder()
        val packet = coder.encoder(this.data!!, this.headers!!)
        val buffer:ByteBuffer = ByteBuffer.allocate(packet.count())
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(packet)
        return buffer
    }*/
}

class AcknowledgmentPacket() :
    Packet {
    override var type: PacketType? = null
    override var headers: Headers? = null
    override var data: ByteArray? = null

    constructor(headers: Headers) : this() {
        this.type = PacketType.acknowledgment
        this.data = null
        this.headers = headers
    }

    constructor(packet: DataPacket) : this() {
        this.type = PacketType.acknowledgment
        this.data = null

        this.headers = Headers(
            ContentType.acknowledgement,
            packet.headers!!.messageNumber,
            packet.headers!!.messageSize,
            packet.headers!!.packetsCount,
            packet.headers!!.packetNumber,
            0,
            packet.headers!!.shift,
            packet.headers!!.firstSize,
            packet.headers!!.secondSize,
            packet.headers!!.sessionID
        )
    }

    override fun encode(): DatagramPacket {
        val coder = Coder()
        val packet = coder.encoderAcknowledgment(this.headers!!)
        return DatagramPacket(packet, packet!!.size)
    }
   /* override fun encode(): ByteBuffer {
        val coder = Coder()
        val packet = coder.encoderAcknowledgment(this.headers!!)
        val buffer:ByteBuffer = ByteBuffer.allocate(packet!!.count())

        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(packet)
        return buffer
    }*/
}

class ConnectionPacket : Packet {
    override var type: PacketType? = null
    override var headers: Headers? = null
    override var data: ByteArray? = null

    constructor(headers: Headers) {
        this.data = null
        this.headers = headers
    }

    constructor(messageNumber: Long, sessionID: String?) {
        this.data = null
        this.headers = Headers(
            ContentType.connection,
            messageNumber,
            0,
            1,
            1,
            0,
            0,
            0,
            0,
            sessionID
        )
    }
    override fun encode(): DatagramPacket {
        val coder = Coder()
        val packet = coder.encoder(null, this.headers!!)
        return DatagramPacket(packet, packet.size)
    }
 /* override fun encode(): ByteBuffer {
      val coder = Coder()
      val packet = coder.encoder(null, this.headers!!)
      val buffer:ByteBuffer = ByteBuffer.allocate(packet.count())

      buffer.order(ByteOrder.LITTLE_ENDIAN)
      buffer.put(packet)
      return buffer
  }*/
}
