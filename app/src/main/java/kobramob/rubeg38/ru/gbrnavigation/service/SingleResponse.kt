package kobramob.rubeg38.ru.gbrnavigation.service

import java.net.DatagramPacket
import java.net.DatagramSocket

class SingleResponse {

    val coder: Coder = Coder()

    fun serverReceiver(socket: DatagramSocket): String {
        val ipAddress = "192.168.2.110"
        val receiverBuffer: ByteArray = kotlin.ByteArray(1041)
        val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
        socket.receive(receiverPacket)
        return if (!receiverPacket.data.isEmpty()) {
            coder.decoderPacketOne(receiverPacket.data)
        } else {
            "Нулевой"
        }
    }
}