package kobramob.rubeg38.ru.gbrnavigation.service

import java.net.DatagramPacket
import java.net.InetAddress
import java.util.*

enum class PacketType {
    connection, acknowledgment, data
}

interface Packet {
    var type: PacketType?
    var headers: Headers?
    var data: ByteArray?
    fun encode(ip:String?,port:Int): DatagramPacket
}

class PacketUtils {

    companion object {
        fun decode(data: DatagramPacket): Packet {

            if (data.length == 0) {
                return ConnectionPacket()
            }
            val coder = Coder()
            val dataArray: ByteArray = data.data

            val (headers, body) = coder.decoder(dataArray)

            if (headers.contentType == ContentType.empty) {
                return AcknowledgmentPacket(headers)
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

    constructor(data: ByteArray, headers: Headers) : this() {
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

    override fun encode(ip: String?, port: Int): DatagramPacket {
        this.type = PacketType.data

        val coder = Coder()
        println("encode")
        val packet = coder.encoder(this.data!!, this.headers!!)
        println(coder.decoder(data!!))
        return DatagramPacket(packet, packet.size, InetAddress.getByName(ip), port)
    }
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
            ContentType.empty,
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

    override fun encode(ip:String?,port:Int): DatagramPacket {
        val coder = Coder()
        val packet = coder.encoderAcknowledgment(this.headers!!)
        return DatagramPacket(packet, packet!!.size, InetAddress.getByName(ip), port)
    }
}

class ConnectionPacket : Packet {
    override var type: PacketType? = null
    override var headers: Headers? = null
    override var data: ByteArray? = null

    init {
        this.type = PacketType.connection
        this.data = null
        this.headers = null
    }
    override fun encode(ip: String?, port: Int): DatagramPacket {
        val arrayBuffer = ByteArray(0)
        return DatagramPacket(arrayBuffer, 0, InetAddress.getByName(ip), port)
    }
}
