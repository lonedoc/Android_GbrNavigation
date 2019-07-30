package kobramob.rubeg38.ru.gbrnavigation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.LongSparseArray
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.OldLoginActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import kotlin.concurrent.thread
import org.json.JSONObject

class NetworkService : Service(), NetworkServiceDelegate {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun messageReceived(message: ByteArray) {
        Log.d("MessageReceiver", Arrays.toString(message))
        Log.d("MessageReceiver", message.count().toString())

        byteMessageBroker.add(message)
    }

    private var waitPicture = false

    override fun messageReceived(message: String) {
        Log.d("MessageReceiver", message)


        try{
            when(JSONObject(message).getString("\$c$")){
                "sendfile"->{
                      /*  val jsonObject = JSONObject()
                        jsonObject.put("\$c$", "getfileok")
                        jsonObject.put("name", jsonObject.getString("name"))
                        send(message = jsonObject.toString(), sessionID = thisSessionId) {
                            if (it) {
                                Log.d("MessageReceiver", "PictureReceive")
                                waitPicture = false
                            }
                        }*/
                }
                else->{}
            }
        }catch (e:Exception){
        }

        try {
            if (!StartActivity.Alive && !OldLoginActivity.Alive && !ObjectActivity.Alive) {
                when (JSONObject(message).getString("command")) {
                    "alarm" -> {
                    }
                    "gbrstatus" -> {
                    }

                    "notalarm" -> {
                    }
                }
            }
        } catch (e: Exception) {
        }

        try {
            if (JSONObject(message).getString("command") == "gbrstatus") {
                for (i in 0 until stringMessageBroker.count()) {
                    if (JSONObject(stringMessageBroker[i]).getString("command") == "gbrstatus") {
                        stringMessageBroker.removeAt(i)
                        stringMessageBroker.add(message)
                        return
                    }
                }
                stringMessageBroker.add(message)
            } else {
                if (JSONObject(message).getString("command") == "regok") {
                    if (lostConnected) {

                        for (i in 0 until stringMessageBroker.count()) {
                            if (JSONObject(stringMessageBroker[i]).getString("command") == "disconnected") {
                                stringMessageBroker.removeAt(i)
                            }
                        }

                        thisSessionId = JSONObject(message).getString("tid")
                        val reconnectMessage = JSONObject()
                        reconnectMessage.put("command", "reconnection")
                        stringMessageBroker.add(reconnectMessage.toString())
                        lostConnected = false
                    } else {
                        stringMessageBroker.add(message)
                    }
                } else {
                    stringMessageBroker.add(message)
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun connectionLost() {
        if (!lostConnected) {
            val disconnectMessage = JSONObject()
            disconnectMessage.put("command", "disconnected")
            stringMessageBroker.add(disconnectMessage.toString())
            lostConnected = true
        } else {
            Log.d("ConnectionLost", attempts.toString())
            if (attempts >= ATTEMPTS_COUNT) {
                attempts = 0
                val sendMessage = JSONObject()
                sendMessage.put("\$c$", "reg")
                sendMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                sendMessage.put("password", thisImei)

                send(sendMessage.toString(), null) {
                    success: Boolean ->
                        if (success) {
                            Log.d("Connected", "true")
                        }
                    }
            }
        }
    }

    private val MAX_PACKETS_COUNT = 36
    private val ATTEMPTS_COUNT = 3
    private val MAX_PACKET_SIZE = 962
    private var MAX_SLEEP_TIME = 0

    private var outcommingMessagesCount: Long = 0
    private var incommingMessagesCount: Long = 0

    private var onAirPackets = CopyOnWriteArrayList<Triple<Packet, Long, Int>>()

    private var lastRequestTime: Long = System.currentTimeMillis()
    private var lastResponseTime: Long = System.currentTimeMillis()
    private var connected: Boolean = false
    private var lostConnected = false
    private var attempts = 0

    var channelId: String? = null

    companion object {

        var socket: DatagramSocket = DatagramSocket(null)

        val packetsToSend = PriorityQueue<Packet>()

        var outcomingTransmissions = LongSparseArray<OutcomingTransmission>()
        var incomingTransmissions = LongSparseArray<IncomingTransmission>()

        var stringMessageBroker: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

        var byteMessageBroker: CopyOnWriteArrayList<ByteArray> = CopyOnWriteArrayList()


        var serviceAlive = true
        var thisSessionId: String? = null
        var thisImei: String = ""
    }

    private var context: Context? = null

    private fun startForeground() {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelId!!)
        val notification =
            notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSubText("Кобра ГБР")
                .setContentTitle("Поддерживаем соединение с сервером")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(channelId!!.toInt(), notification)

        startService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelID = "101"
        val channelName = "MyBackgroundService"
        val chan = NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.GRAY
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelID
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
        } else {
            startService()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceAlive = false
        stopForeground(false)
        stopSelf()
    }

    fun initSocket(ip: String?, port: Int) {
        try {
            socket.reuseAddress = true
            socket.connect(InetSocketAddress(ip, port))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initData(sessionID: String?, imei: String, context: Context) {
        thisSessionId = sessionID
        thisImei = imei
        this.context = context
    }

    private fun startService() {
        lastResponseTime = System.currentTimeMillis()
        lastRequestTime = System.currentTimeMillis()
        serviceAlive = true

        sendLoop()

        receiverLoop()

        connectionLoop()

        coordinateLoop()
    }

    fun request(data: ByteArray, sessionID: String?, responseHandler: ResponseHandler) {
        this.createPacketOnQueue(
            data = data,
            sessionID = thisSessionId,
            contentType = ContentType.binary,
            isWaitingForResponse = true,
            responseHandler = responseHandler
        )
    }

    fun request(message: String, sessionID: String?, responseHandler: ResponseHandler) {
        val data = message.toByteArray()
        this.createPacketOnQueue(
            data = data,
            sessionID = thisSessionId,
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
            sessionID = thisSessionId,
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
            sessionID = thisSessionId,
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
            Log.d("countSubPacket",subPacketNumber.toString())
        }
    }

    private fun coordinateLoop() {
        thread {
            var speed:Float = 0.toFloat()
            while (serviceAlive) {
                if (MyLocation.imHere != null) {
                    if(MyLocation.imHere?.speed != speed)
                    {
                        val message = JSONObject()
                        message.put("\$c$", "gbrkobra")
                        message.put("command", "location")
                        message.put("id", thisImei)
                        message.put("lon", MyLocation.imHere?.longitude)
                        message.put("lat", MyLocation.imHere?.latitude)
                        message.put("speed", MyLocation.imHere?.speed)
                        send(message.toString(), thisSessionId) {
                            if (it) {
                                Log.d("Coordinate", "CoordinatePacket")
                            } else
                                connectionLost()
                        }
                        speed = MyLocation.imHere?.speed!!
                    }

                    sleep(8000)
                }
            }
        }
    }

    private fun connectionLoop() {
        thread {
            while (serviceAlive) {

                if (lastRequestTime + 5000 <= System.currentTimeMillis()) {
                    outcommingMessagesCount += 1
                    val messageNumber = outcommingMessagesCount
                    val sessionID = thisSessionId
                    val packet = ConnectionPacket(messageNumber = messageNumber, sessionID = sessionID)
                    packetsToSend.enqueue(packet, 3)
                }

                sleep(7000)
            }
        }
    }

    private fun sendLoop() {
        thread {
            while (serviceAlive) {
                sleep(MAX_SLEEP_TIME.toLong())
                val time = System.currentTimeMillis()
                for (i in 0 until this.onAirPackets.count()) {
                    try {
                        if (this.onAirPackets[i].second <= time) {
                            Log.d("Send", "PacketRetransmitted")

                            val packet = this.onAirPackets[i].first

                            Log.d("PacketRetransmitted",String(packet.data!!))
                            val timeOnAir = this.onAirPackets[i].second
                            val count = this.onAirPackets[i].third

                            this.onAirPackets.removeAt(i)

                            try{
                                if(JSONObject(String(packet.data!!)).getString("\$c$") == "getfile")
                                {
                                    continue
                                }
                            }catch (e:Exception){
                            }


                            this.onAirPackets.add(i, Triple(packet, timeOnAir, count + 1))

                            if (this.onAirPackets[i].third> this.MAX_PACKETS_COUNT) {
                                Log.d("Send", "PacketFailed")
                                continue
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // remove packet
                val failedPacket = this.onAirPackets.filter { it.third > this.ATTEMPTS_COUNT }

                for (i in 0 until failedPacket.size) {
                    val messageNumber = failedPacket[i].first.headers!!.messageNumber
                    packetsToSend.removeAll { it.headers!!.messageNumber == messageNumber }
                    try {
                        outcomingTransmissions[messageNumber].responseHandler(false, null)
                        outcomingTransmissions.remove(messageNumber)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                try {
                    onAirPackets.removeAll { it.third > this.ATTEMPTS_COUNT }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                // add new packet
                try {
                    if (this.onAirPackets.count() <this.MAX_PACKETS_COUNT) {
                        val packet: Packet?

                        if (packetsToSend.dequeue() != null) {
                            packet = packetsToSend.dequeue()
                            packetsToSend.remove()

                            if (packet!!.headers!!.contentType == ContentType.string ||
                                packet.headers!!.contentType == ContentType.binary
                            ) {
                                Log.d("Send", "Packet")
                                this.onAirPackets.add(Triple(packet, time + 10000, 1))
                            } else {
                                if (packet.headers!!.contentType == ContentType.acknowledgement) {
                                    Log.d("Send", "Acknowledgement")
                                } else {
                                    Log.d("Send", "ConnectionPacket")
                                }
                            }

                            try {
                                socket.send(packet.encode())
                                this.lastRequestTime = System.currentTimeMillis()
                            } catch (e: Exception) {
                                Log.d("Socket", "SendFailed")
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                MAX_SLEEP_TIME = if(packetsToSend.countToQueue()==0){
                    500
                } else {
                    0
                }
            }
        }
    }

    private fun receiverLoop() {
        thread {
            socket.soTimeout = 1000
            while (serviceAlive) {
                if (lastResponseTime + 10000 < System.currentTimeMillis()) {
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

                    socket.receive(receiverPacket)

                    if (receiverBuffer.count { it> 0 } == 0) {
                        continue
                    }
                } catch (e: Exception) {
                    continue
                }

                connected = true
                lastResponseTime = System.currentTimeMillis() + 1000

                val packet = PacketUtils.decode(receiverPacket)

                Log.d("ReceiverInfo", packet.headers!!.contentType.toString())

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
                try {
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
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun handleDataPacket(packet: DataPacket) {
        val acknowledgment = AcknowledgmentPacket(packet = packet)
        packetsToSend.enqueue(item = acknowledgment, priority = 1)

        val messageNumber = packet.headers!!.messageNumber

        if (messageNumber <incommingMessagesCount && incomingTransmissions[messageNumber] == null)
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