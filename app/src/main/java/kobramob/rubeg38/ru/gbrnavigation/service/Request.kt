package kobramob.rubeg38.ru.gbrnavigation.service

import android.util.Log
import org.json.JSONObject
import java.net.*
import java.nio.ByteBuffer

class Request {

    private val coder: Coder = Coder()
    /*private val ipAddress = "192.168.2.110"*/
    private val ipAddress = "194.146.201.66"
    private val maxSizePacket = 1057
    private val port = 9010
    private var register = false

    fun register(socket: DatagramSocket, count: Long, typePacket: Byte, imei: String, ip: String, port: Int, tid: String) {
        val message = JSONObject()
        message.put("\$c$", "reg")
        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        message.put("password", imei)

        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), count, typePacket, tid)
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size, InetAddress.getByName(ip), port)
        socket.send(sendPacket)
    }

    fun packetType255(
        socket: DatagramSocket,
        message: ByteArray,
        port: Int,
        address: InetAddress
    ) {
        val buffer255: ByteBuffer = coder.packetType255(message)
        val sendPacket = DatagramPacket(buffer255.array(), buffer255.array().size, address, port)
        socket.send(sendPacket)
    }

    fun nullPacket(socket: DatagramSocket, ip: String?, port: Int) {
        val sendData = ByteArray(0)
        val nullPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(ip), port)
        socket.send(nullPacket)
    }

    private fun onePacket(
        encoderPacket: ByteArray?,
        socket: DatagramSocket,
        ip: String,
        port: Int
    ): String {
        try {
            socket.soTimeout = 10000
            println("Регистрация")
            println(ip)
            println(port)
            val sendPacket = DatagramPacket(
                encoderPacket, encoderPacket!!.size, InetAddress.getByName(ip),
                port
            )
            socket.send(sendPacket)
            val receiverBuffer = ByteArray(maxSizePacket)
            var requestServer = "TimeOut"
            do {
                val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                socket.receive(receiverPacket)
                when (coder.typePacket(receiverPacket.data)) {
                    255 -> {
                        println("Мой пакет принят")
                    }
                    0 -> {
                        packetType255(socket, receiverPacket.data, receiverPacket.port, receiverPacket.address)
                        requestServer = coder.decoderPacketOne(receiverPacket.data)
                        register = true
                        return requestServer
                    }
                }
            } while (!register)
            return requestServer
        } catch (e: Exception) {
            e.printStackTrace()
            return "TimeOut"
        }
    }

    fun sendLocation(
        socket: DatagramSocket,
        ip: String?,
        port: Int,
        count: Long,
        typePacket: Byte,
        latitude: Double,
        longitude: Double,
        speed: Float,
        tid: String?,
        imei: String?
    ) {
        val message = JSONObject()
        message.put("\$c$", "gbrkobra")
        message.put("command", "location")
        message.put("id", imei)
        message.put("lon", longitude)
        message.put("lat", latitude)
        message.put("speed", speed)

        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), count, typePacket, tid.toString())
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size, InetAddress.getByName(ip), port)
        socket.send(sendPacket)
    }

    fun changeStatus(
        socket: DatagramSocket,
        status: String?,
        countSender: Long,
        typePacket: Byte,
        imei: String?,
        ip: String?,
        port: Int,
        tid: String?
    ) {
        val message = JSONObject()
        message.put("\$c$", "gbrkobra")
        message.put("command", "status")
        message.put("newstatus", status)
        println(message.toString())
        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), countSender, typePacket, tid.toString())
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size, InetAddress.getByName(ip), port)
        socket.send(sendPacket)
    }

    fun acceptAlarm(
        socket: DatagramSocket,
        countSender: Long,
        number: String?,
        typePacket: Byte,
        imei: String?,
        ip: String?,
        port: Int,
        tid: String?
    ) {
        val message = JSONObject()
        message.put("\$c$", "gbrkobra")
        message.put("command", "alarmp")
        message.put("newstatus", number)
        println(message.toString())
        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), countSender, typePacket, tid.toString())
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size, InetAddress.getByName(ip), port)
        socket.send(sendPacket)
    }

    fun arrivedToObject(
        socket: DatagramSocket,
        countSender: Long,
        number: String?,
        typePacket: Byte,
        imei: String?,
        ip: String?,
        port: Int,
        tid: String?
    ) {
        val message = JSONObject()
        message.put("\$c$", "gbrkobra")
        message.put("command", "alarmpr")
        message.put("newstatus", number)
        println(message.toString())
        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), countSender, typePacket, tid.toString())
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size, InetAddress.getByName(ip), port)
        socket.send(sendPacket)
    }
}
