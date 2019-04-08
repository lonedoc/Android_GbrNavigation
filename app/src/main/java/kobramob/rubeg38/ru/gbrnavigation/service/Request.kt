package kobramob.rubeg38.ru.gbrnavigation.service

import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.*

class Request {
    // private val headSize = 21

    private val coder: Coder = Coder()

    private val sendPacket = 87000
    private val sizePacket = 1500
    private val headSize = 36
    private fun serverCom(decoder: ByteBuffer, socket: DatagramSocket): String {

        val ipAddress = "192.168.2.110"
        // val ipAddr = byteArrayOf(192.toByte(), 168.toByte(), 2, 77)
        val sendPacket: DatagramPacket = DatagramPacket(
            decoder.array(), decoder.array().size,
            InetAddress.getByName(ipAddress), 8301
        )

        println(sendPacket.socketAddress)

        socket.send(sendPacket)

/*
        val receiverBuffer:ByteArray = kotlin.ByteArray(1041)
        val receiverPacket = DatagramPacket(receiverBuffer,receiverBuffer.size)
        socket.receive(receiverPacket)
        System.out.println(Arrays.toString(receiverPacket.data))*/

        /* val ipAddr = byteArrayOf(192.toByte(), 168.toByte(), 2, 110)
         var datagramPacket:DatagramPacket = DatagramPacket(decoder!!.array(), decoder.array().size, InetAddress.getByAddress(ipAddr),8301)
         val datagramSocket:DatagramSocket = DatagramSocket()
         datagramSocket.send(datagramPacket)
         val buffer:ByteArray = ByteArray(41)
         datagramPacket = DatagramPacket(buffer,buffer.size)
         datagramSocket.receive(datagramPacket)
         println(datagramPacket.length)
         coder.encoderHead(datagramPacket.data)
         System.out.println(coder.calculateSize(datagramPacket.data))
         var head:ByteArray = ByteArray(41)
         head = datagramPacket.data
         coder.encoderHead(head)
         val packageSize = coder.calculateSize(head)
         val received = String(
             datagramPacket.data, 41, datagramPacket.length
         )
         var pack:ByteArray = ByteArray(1000)
         System.arraycopy(datagramPacket.data,41,pack,0,datagramPacket.length-40)*/

        return "отправил"
    }

    fun register(socket: DatagramSocket): String {

        val message = JSONObject()
        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        message.put("username", "ГБР_103")
        message.put("password", "864799036698001")

        return serverCom(coder.decoderOne(message.toString()), socket)
    }
}