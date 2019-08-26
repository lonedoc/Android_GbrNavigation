package kobramob.rubeg38.ru.networkprotocol

import android.util.Log
import java.nio.ByteBuffer
import java.util.*

class IncomingTransmission {
    private var packets: BooleanArray?
    private var data: ByteBuffer?
    var timeToFail: Long
    var responseHandler: ResponseHandler

    constructor(responseHandler: ResponseHandler) {
        this.packets = null
        this.data = null

        this.timeToFail = System.currentTimeMillis() + 30000
        this.responseHandler = responseHandler
    }

    val done: Boolean
    get() = this.packets?.all { it } ?: false

    val failed: Boolean
    get() = this.timeToFail < System.currentTimeMillis()

    val message: ByteArray?
    get() = if (this.done) this.data?.array() else null

    val percent:Int
    get() = (packets!!.count { it }*100)/packets!!.count()

    var index = 0
    fun addPacket(packet: Packet) {

        index++

        Log.d("IncomingTransmission",
            "Index: $index \n" +
                    "Message size: ${packet.headers.messageSize} \n" +
                    "Packets count: ${packet.headers.packetsCount} \n " +
                    "Packet number: ${packet.headers.packetNumber} \n"  +
                    "Shift: ${packet.headers.shift} \n" +
                    "Data count: ${packet.data?.count()}")

        if (this.packets == null)
            this.packets = BooleanArray(packet.headers.packetsCount)

        if (this.data == null)
            this.data = ByteBuffer.allocate(packet.headers.messageSize)

        this.data!!.position(packet.headers.shift)
        this.data!!.put(packet.data!!)

        this.packets!![packet.headers.packetNumber - 1] = true

        Log.d("IncomingTransmission","Packets: ${Arrays.toString(packets)}")

        this.timeToFail = System.currentTimeMillis() + 30000
    }
}
