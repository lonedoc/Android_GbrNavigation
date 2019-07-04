package kobramob.rubeg38.ru.gbrnavigation.service

import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IncomingTransmission() {
    private var packets: BooleanArray? = null
    private var data: ByteBuffer? = null
    var timeToFail: Long = 0
        private set
    lateinit var responseHandler: ResponseHandler
        private set

    constructor(responseHandler: ResponseHandler) : this() {
        this.responseHandler = responseHandler
        this.timeToFail = System.currentTimeMillis() + 30000
    }

    var done: () -> Boolean = {
        if (packets == null)
            false
        else
            packets!!.all { it }
    }

    var failed: () -> Boolean = {
        this.timeToFail < System.currentTimeMillis()
    }

    var message: () -> ByteArray? = {
        if (done()) {
            this.data!!.array()
        } else {
            null
        }
    }

    fun addPacket(packet: Packet) {
        if (this.packets == null) {
            packets = BooleanArray(packet.headers!!.packetsCount)
        }
        if (this.data == null) {
            this.data = ByteBuffer.allocate(packet.headers!!.messageSize)
            data!!.order(ByteOrder.LITTLE_ENDIAN)
        }
        try {
            this.data!!.position(packet.headers!!.shift)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        this.data!!.put(packet.data)
        this.packets!![packet.headers!!.packetNumber - 1] = true
        this.timeToFail = System.currentTimeMillis() + 30000
    }
}