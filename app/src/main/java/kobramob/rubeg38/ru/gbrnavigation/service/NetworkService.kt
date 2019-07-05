package kobramob.rubeg38.ru.gbrnavigation.service

import android.content.Context
import android.location.Location
import android.util.Log
import android.util.LongSparseArray
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread


class NetworkService:NetworkServiceDelegate {

    override fun messageReceived(message: ByteArray) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun messageReceived(message: String) {
        Log.d("MessageReceiver",message)

        if(!StartActivity.Alive && !LoginActivity.Alive && !ObjectActivity.Alive){
            when(JSONObject(message).getString("command")){
                "alarm"->{

                }
                "gbrstatus"->{

                }

                "notalarm"->{

                }
            }
        }

        if(JSONObject(message).getString("command")=="gbrstatus"){
            for(i in 0 until  messageBroker.count()){
                if(JSONObject(messageBroker[i]).getString("command")=="gbrstatus")
                {
                    messageBroker.removeAt(i)
                }
            }
            messageBroker.add(message)
        }
        else
        {
            if(JSONObject(message).getString("command")=="regok"){
                if(lostConnected) {
                    this.sessionID = JSONObject(message).getString("tid")
                    val reconnectMessage = JSONObject()
                    reconnectMessage.put("command", "reconnection")
                    messageBroker.add(reconnectMessage.toString())
                    lostConnected=false
                }
            }
            else{
                messageBroker.add(message)
            }

        }
    }



    override fun connectionLost() {
        if(!lostConnected){
            val disconnectMessage = JSONObject()
            disconnectMessage.put("command","disconnected")
            messageBroker.add(disconnectMessage.toString())
            lostConnected = true

        }
        else
        {
            Log.d("ConnectionLost",attempts.toString())
            if(attempts>=ATTEMPTS_COUNT){
                attempts = 0
                val sendMessage = JSONObject()
                sendMessage.put("\$c$", "reg")
                sendMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                sendMessage.put("password", imei)

                send(sendMessage.toString(),null){
                        success:Boolean->
                    if (success){
                        Log.d("Connected","true")
                    }
                }
            }

        }

    }

    private val MAX_PACKETS_COUNT = 20
    private val ATTEMPTS_COUNT = 3
    private val MAX_PACKET_SIZE = 962

    private var outcommingMessagesCount: Long = 0
    private var incommingMessagesCount: Long = 0

    private var onAirPackets = CopyOnWriteArrayList<Triple<Packet, Long, Int>>()

    private var lastRequestTime: Long = System.currentTimeMillis()
    private var lastResponseTime: Long = System.currentTimeMillis()
    private var connected: Boolean = false
    private var lostConnected = false
    private var attempts = 0

    var channelId:String? = null
    companion object {

        var currentLocation: Location? = null
        lateinit var socket: DatagramSocket
        val packetsToSend = PriorityQueue<Packet>()

        var outcomingTransmissions = LongSparseArray<OutcomingTransmission>()
        var incomingTransmissions = LongSparseArray<IncomingTransmission>()

        var messageBroker:CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    }
    private var sessionID:String? = null
    private var imei:String = ""
    private var context:Context? = null
    fun initSocket(ip: String?, port: Int) {
        try {
            socket = DatagramSocket(null)
            socket.reuseAddress = true
            socket.connect(InetSocketAddress(ip, port))
//            socket.bind(InetSocketAddress(socket.localPort))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startService() {
        lastResponseTime = System.currentTimeMillis()
        lastRequestTime = System.currentTimeMillis()
        sendLoop()

        receiverLoop()

        connectionLoop()
    }

    fun initData(sessionID: String?, imei: String,context:Context){
        this.sessionID = sessionID
        this.imei = imei
        this.context = context
    }

    fun request(data: ByteArray, sessionID: String?, responseHandler: ResponseHandler) {
        this.createPacketOnQueue(
            data = data,
            sessionID = sessionID,
            contentType = ContentType.binary,
            isWaitingForResponse = true,
            responseHandler = responseHandler
        )
    }

    fun request(message: String, sessionID: String?, responseHandler: ResponseHandler) {
        val data = message.toByteArray()
        this.createPacketOnQueue(
            data = data,
            sessionID = sessionID,
            contentType = ContentType.string,
            isWaitingForResponse = true,
            responseHandler = responseHandler
        )
    }

    fun send(
        message: String,
        sessionID: String?,
        resultHandler: ResultHandler
    ) {
        val data = message.toByteArray()
        this.createPacketOnQueue(
            data = data,
            sessionID = sessionID,
            contentType = ContentType.string,
            isWaitingForResponse = false
        ) { succes: Boolean, _: ByteArray? ->
            resultHandler(succes)
        }
    }

    fun send(
        data: ByteArray,
        sessionID: String?,
        resultHandler: ResultHandler
    ) {
        this.createPacketOnQueue(
            data = data,
            sessionID = sessionID,
            contentType = ContentType.binary,
            isWaitingForResponse = false
        ) { succes: Boolean, _: ByteArray? ->
            resultHandler(succes)
        }
    }

    private fun createPacketOnQueue(data: ByteArray, sessionID: String?, contentType: ContentType, isWaitingForResponse: Boolean, responseHandler: ResponseHandler) {

        thread {
            this.outcommingMessagesCount += 1

            val messageNumber = this.outcommingMessagesCount

            var packetCount = data.size / MAX_PACKET_SIZE
            if (data.size % MAX_PACKET_SIZE != 0) {
                packetCount += 1
            }

            val booleanArray = BooleanArray(packetCount)
            for (i in 0 until packetCount) {
                booleanArray[i] = false
            }

            outcomingTransmissions.put(messageNumber, OutcomingTransmission(packetCount, isWaitingForResponse, responseHandler))

            var subPacketNumber = 1
            var leftBound = 0
            while (leftBound <data.count()) {
                val rightBound = if (leftBound + 962 <data.count()) {
                    leftBound + 962
                } else
                    data.count()

                val chunk = data.slice(IntRange(leftBound, rightBound - 1))

                val packet = DataPacket(
                    data = chunk.toByteArray(),
                    sessionID = sessionID,
                    contentType = contentType,
                    messageNumber = messageNumber,
                    messageSize = data.count(),
                    shift = leftBound,
                    packetCount = packetCount,
                    packetNumber = subPacketNumber
                )

                packetsToSend.enqueue(packet)

                subPacketNumber++
                leftBound += MAX_PACKET_SIZE
            }
        }
    }

    @Synchronized private fun connectionLoop() {
        thread {
            while (true) {
                if (lastRequestTime + 5000 <= System.currentTimeMillis()) {
                    outcommingMessagesCount += 1
                    val messageNumber = outcommingMessagesCount
                    val sessionID = sessionID
                    val packet = ConnectionPacket(messageNumber = messageNumber, sessionID = sessionID)
                    packetsToSend.enqueue(packet, 3)
                }
                Thread.sleep(6000)
            }
        }
    }

    @Synchronized private fun sendLoop() {
        thread {
            while (true) {
                val time = System.currentTimeMillis()
                for (i in 0 until this.onAirPackets.count()) {
                    try {
                        if (this.onAirPackets[i].second <= time) {
                            Log.d("SendLoop", "PacketRetransmitted")

                            val packet = this.onAirPackets[i].first
                            val timeOnAir = this.onAirPackets[i].second
                            val count = this.onAirPackets[i].third

                            this.onAirPackets.removeAt(i)

                            this.onAirPackets.add(i, Triple(packet, timeOnAir, count + 1))

                            if (this.onAirPackets[i].third> this.MAX_PACKETS_COUNT) {
                                Log.d("SendLoop", "PacketFailed")
                                continue
                            }

                            if(lostConnected){
                                attempts++
                                Log.d("Attempts",attempts.toString())
                            }
                            val packet1 = this.onAirPackets[i].first
                            val timeOnAirPlus10 = timeOnAir + 10000
                            val count1 = this.onAirPackets[i].third
                            this.onAirPackets.removeAt(i)

                            this.onAirPackets.add(i, Triple(packet1, timeOnAirPlus10, count1))

                            try {
                                socket.send(onAirPackets[i].first.encode())
                                this.lastRequestTime = System.currentTimeMillis()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                connectionLost()
                            }
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }
                // remove packet
                val failedPacket = this.onAirPackets.filter { it.third > this.ATTEMPTS_COUNT }

                for (i in 0 until failedPacket.size) {
                    val messageNumber = failedPacket[i].first.headers!!.messageNumber
                    packetsToSend.removeAll { it.headers!!.messageNumber == messageNumber }
                    outcomingTransmissions[messageNumber].responseHandler(false, null)
                    outcomingTransmissions.remove(messageNumber)
                }

                try {
                    onAirPackets.removeAll { it.third > this.ATTEMPTS_COUNT }
                }catch (e: Exception){
                    e.printStackTrace()
                }

                // add new packet
                try {
                    if (this.onAirPackets.count() <this.MAX_PACKETS_COUNT) {
                        val packet: Packet?

                        if (packetsToSend.dequeue() != null) {
                            Log.d("SendLoop", "AddNewPacket")

                            packet = packetsToSend.dequeue()
                            packetsToSend.remove()

                            if (packet!!.headers!!.contentType == ContentType.string ||
                                packet!!.headers!!.contentType == ContentType.binary
                            ) {
                                this.onAirPackets.add(Triple(packet, time + 10000, 1))
                            } else {
                                Log.d("SendLoop","ConnectionPacket")
                            }

                            try {
                                socket.send(packet.encode())
                                this.lastRequestTime = System.currentTimeMillis()
                            } catch (e: Exception) {
                                Log.d("Socket", "SendFailed")
                                connectionLost()
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Synchronized private fun receiverLoop() {
        Log.d("Receiver", "Start")
        thread {
            socket.soTimeout = 0
            while (true) {
                if (lastResponseTime + 10000 < System.currentTimeMillis()) {
                    Log.d("Receiver", "ConnectionLost")
                    lastResponseTime = System.currentTimeMillis()
                    connected = false

                    onAirPackets.removeAll { true }
                    incomingTransmissions.clear()
                    outcomingTransmissions.clear()

                    connectionLost()
                }
                // Создаем буффер
                val receiverBuffer = ByteArray(MAX_PACKET_SIZE + 57)
                // Создаем датаграмму для приема
                val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                try {
                    // Принимаем то что лежит в сокете
                    // Теперь могу даже перехватить если упал сокет,каеф
                    socket.receive(receiverPacket)

                    if (receiverPacket.length == 0) {
                        continue
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    attempts++
                    connectionLost()
                    continue
                }

                connected = true
                lastResponseTime = System.currentTimeMillis()

                val packet = PacketUtils.decode(receiverPacket)

                when (packet.headers!!.contentType) {
                    ContentType.acknowledgement -> {
                        handleAcknowledgmentPacket(packet as AcknowledgmentPacket)
                    }
                    ContentType.connection -> {
                        val messageNumber = packet.headers!!.messageNumber
                        if (messageNumber> incommingMessagesCount) {
                            incommingMessagesCount = messageNumber
                        }
                    }
                    else -> {
                        handleDataPacket(packet as DataPacket)
                    }
                }
                // Remove fail packet
                try{
                    for (i in 0 until incomingTransmissions.size()) {
                        if (incomingTransmissions[i.toLong()].timeToFail <= System.currentTimeMillis()) {
                            incomingTransmissions[i.toLong()].responseHandler(false, null)
                        }
                    }
                    for (i in 0 until incomingTransmissions.size()) {
                        if (incomingTransmissions[i.toLong()].timeToFail <= System.currentTimeMillis()) {
                            incomingTransmissions.delete(i.toLong())
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }

            }
        }
    }

    private fun handleDataPacket(packet: DataPacket) {
        val acknowledgment = AcknowledgmentPacket(packet = packet)
        packetsToSend.enqueue(item = acknowledgment, priority = 2)

        val messageNumber = packet.headers!!.messageNumber

        if(messageNumber<incommingMessagesCount && incomingTransmissions[messageNumber]==null)
            return

        if (messageNumber> this.incommingMessagesCount) {
            incommingMessagesCount = messageNumber
        }


        if (incomingTransmissions[messageNumber] == null) {
            val handler: ResponseHandler

            when (packet.headers!!.contentType) {
                ContentType.string -> {

                    handler = handler@{ success: Boolean, data: ByteArray? ->
                        if (!success)
                            return@handler
                        if (data != null) {
                            messageReceived(String(data))
                        }
                    }
                }
                ContentType.binary -> {
                    handler = handler@{ succes: Boolean, data: ByteArray? ->
                        if (succes) {
                            if (data != null)
                                messageReceived(data)
                        } else {
                            return@handler
                        }
                    }
                }
                ContentType.acknowledgement -> TODO()
                ContentType.connection -> TODO()
            }
            incomingTransmissions.put(messageNumber, IncomingTransmission(handler))
        }
        if (incomingTransmissions[messageNumber] != null) {
            val transmission = incomingTransmissions[messageNumber]
            transmission.addPacket(packet)
            if (transmission.done()) {
                transmission.responseHandler(true, transmission.message())
                incomingTransmissions.remove(messageNumber)
            }
        }
    }

    private fun handleAcknowledgmentPacket(packet: AcknowledgmentPacket) {
        for (i in 0 until onAirPackets.count()) {
            val current = onAirPackets[i].first

            val sameMessageNumber = current.headers!!.messageNumber == packet.headers!!.messageNumber
            val samePacketsCount = current.headers!!.packetsCount == packet.headers!!.packetsCount
            val samePacketNumber = current.headers!!.packetNumber == packet.headers!!.packetNumber
            if (sameMessageNumber && samePacketNumber && samePacketsCount) {
                onAirPackets.removeAt(i)
                break
            }
        }

        val messageNumber = packet.headers!!.messageNumber
        val packetNumber = packet.headers!!.packetNumber

        if (outcomingTransmissions[messageNumber] != null) {
            val transmission: OutcomingTransmission = outcomingTransmissions[messageNumber]
            transmission.addAcknowledgements(packetNumber - 1)
            if (transmission.done()) {
                if (transmission.isResponseExpected) {
                    val responseHandler = transmission.responseHandler
                    val expectedNumber = incommingMessagesCount + 1
                    incomingTransmissions.put(expectedNumber, IncomingTransmission(responseHandler))
                } else {
                    transmission.responseHandler(true, null)
                }
                outcomingTransmissions.remove(messageNumber)
            }
        }
    }
}