package ru.rubeg38.rubegprotocol

import java.nio.ByteBuffer

class IncomingTransmission {
    companion object {
        val MESSAGE_DROP_INTERVAL: Long = 30_000
    }

    private var packets: BooleanArray? = null
    private var data: ByteBuffer? = null

    private var deadline: Long = System.currentTimeMillis() + MESSAGE_DROP_INTERVAL

    val done: Boolean
        get() = packets?.all { it } ?: false

    val failed: Boolean
        get() = deadline < System.currentTimeMillis()

    val message: ByteArray?
        get() = if (done) data?.array() else null

    fun addPacket(packet: Packet) {
        if (packets == null)
            packets = BooleanArray(packet.headers.packetsCount)

        if (data == null)
            data = ByteBuffer.allocate(packet.headers.messageSize)

        data!!.position(packet.headers.shift)
        data!!.put(packet.data!!)

        packets!![packet.headers.packetNumber - 1] = true

        deadline = System.currentTimeMillis() + MESSAGE_DROP_INTERVAL
    }
}