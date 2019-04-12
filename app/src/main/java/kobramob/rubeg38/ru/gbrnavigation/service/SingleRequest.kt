package kobramob.rubeg38.ru.gbrnavigation.service

import org.json.JSONObject
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.*

class SingleRequest {
    private val coder: Coder = Coder()
    private val ipAddress = "192.168.2.110"
    private val maxSizePacket = 1041
    private fun serverComOne(decoder: ByteBuffer, socket: DatagramSocket): String {

        val sendPacket: DatagramPacket = DatagramPacket(
            decoder.array(), decoder.array().size,
            InetAddress.getByName(ipAddress), 8301
        )

        socket.send(sendPacket)

        return "отправил"
    }

    private var register = false

    fun register(socket: DatagramSocket, count: Long, typePacket: Byte): String {

        socket.soTimeout = 10000
        try {
            val message = JSONObject()
            message.put("\$c$", "reg")
            message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
            message.put("username", "Сяоми")
            /* message.put("password", "864799036698001")*/
            message.put("password", "352414093774849")

            val decoderPacket: ByteBuffer = coder.decoderOne(message.toString(), count, typePacket)

            println(coder.encoderPacketOne(decoderPacket.array()))
            val sendPacket = DatagramPacket(decoderPacket.array(), decoderPacket.array().size, InetAddress.getByName(ipAddress), 8301)
            socket.send(sendPacket)

            val receiverBuffer: ByteArray = ByteArray(maxSizePacket)
            var requestServer = "TimeOut"
            do {
                val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                socket.receive(receiverPacket)
                when (coder.typePacket(receiverPacket.data)) {
                    255 -> {
                        println("Пакет принят")
                    }
                    0 -> {
                        packetType255(socket, receiverPacket.data)
                        requestServer = coder.encoderPacketOne(receiverPacket.data)
                        register = true
                        return requestServer
                    }
                    else -> {
                        println("Какая то херь из сокета")
                    }
                }
            } while (!register)
            return requestServer
        } catch (e: Exception) {
            e.printStackTrace()
            return "TimeOut"
        }
    }

    fun packetType255(socket: DatagramSocket, message: ByteArray) {
        val buffer255: ByteBuffer = coder.packetType255(message)
        val sendPacket = DatagramPacket(buffer255.array(), buffer255.array().size, InetAddress.getByName(ipAddress), 8301)
        socket.send(sendPacket)
    }

    fun nullPacket(socket: DatagramSocket) {
        val sendData = ByteArray(0)
        val nullPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(ipAddress), 8301)
        socket.send(nullPacket)
    }
}