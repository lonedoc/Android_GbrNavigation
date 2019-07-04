package kobramob.rubeg38.ru.gbrnavigation.service

import org.json.JSONObject
import java.net.*
import java.nio.ByteBuffer
import java.util.*

class Request {

    private val coder: Coder = Coder()

    fun register(socket: DatagramSocket, count: Long, typePacket: Byte, imei: String, ip: String, port: Int, tid: String) {
        val message = JSONObject()
        message.put("\$c$", "reg")
        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        message.put("password", imei)
        println(message.toString())
        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), count, typePacket, tid)
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size)
        socket.send(sendPacket)
    }

    fun packetType255(
        socket: DatagramSocket,
        message: ByteArray
    ) {
        val buffer255: ByteBuffer = coder.packetType255(message)
        val sendPacket = DatagramPacket(buffer255.array(), buffer255.array().size)
        socket.send(sendPacket)
    }

    fun packet254(socket: DatagramSocket, tid: String?, ip: String?, port: Int) {
        val sendData = coder.packetType254(tid.toString())
        val nullPacket = DatagramPacket(sendData.array(), sendData.array().size)
        socket.send(nullPacket)
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
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size)
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
        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), countSender, typePacket, tid.toString())
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size)
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
        message.put("number", number)
        println(message.toString())
        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), countSender, typePacket, tid.toString())
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size)
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
        message.put("number", number)
        println(message.toString())
        val encoderPacket: ByteBuffer = coder.encoderOne(message.toString(), countSender, typePacket, tid.toString())
        val sendPacket = DatagramPacket(encoderPacket.array(), encoderPacket.array().size)
        socket.send(sendPacket)
    }
}
