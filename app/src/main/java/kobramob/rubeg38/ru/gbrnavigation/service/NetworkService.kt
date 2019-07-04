package kobramob.rubeg38.ru.gbrnavigation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.util.LongSparseArray
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.ContentType.*
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.sql.Time
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

class NetworkService : Service(), NetworkServiceDelegate {
    override var sessionID: String
        get() = TODO("not implemented") // To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun messageReceived(message: ByteArray) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun messageReceived(message: String) {

        Log.d("StringMessageReceiver",message)

        val jsonMessage = JSONObject(message)
        var intent: Intent?
        when(jsonMessage.getString("command")){
            "gbrstatus"->{
                when{
                    StartActivity.Alive->{
                        intent = Intent(StartActivity.BROADCAST_ACTION)
                        intent.putExtra("status",jsonMessage.getString("status"))
                        sendBroadcast(intent)
                    }
                    ObjectActivity.Alive->{
                        intent = Intent(ObjectActivity.BROADCAST_ACTION)
                        intent.putExtra("status",jsonMessage.getString("status"))
                        sendBroadcast(intent)
                    }
                    else->{
                        SharedPreferencesState.init(this@NetworkService)
                        SharedPreferencesState.addPropertyString(
                            "status",
                            jsonMessage.getString("status")
                        )
                    }
                }
            }
            "alarm"->{
               /* if(!StartActivity.Alive){

                    val notificationBuilder = NotificationCompat.Builder(this, "alarm")
                    val notification =
                        notificationBuilder.setOngoing(true)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setSubText("Тревога")
                            .setContentTitle(jsonMessage.getString("name"))
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(Notification.CATEGORY_ALARM)
                            .setUsesChronometer(true)
                            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                            .build()

                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(1, notification)
                }*/

                intent = Intent(StartActivity.BROADCAST_ACTION)
                intent.putExtra("alarm",message)
                sendBroadcast(intent)

            }
            "regok"->{
                when{
                    StartActivity.Alive->{
                        intent = Intent(StartActivity.BROADCAST_ACTION)
                        intent.putExtra("regok","ok")
                        sendBroadcast(intent)
                    }
                    ObjectActivity.Alive->{
                        intent = Intent(StartActivity.BROADCAST_ACTION)
                        intent.putExtra("regok","ok")
                        sendBroadcast(intent)
                    }
                    else->{
                        val message = JSONObject()
                        message.put("\$c$", "reg")
                        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                        message.put("password", getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei",""))

                        request(message = message.toString(), sessionID = null) { success: Boolean, data: ByteArray? ->
                            if (success) {
                                Log.d("Authorization", "success")

                                if (data != null) {

                                    val jsonObject = JSONObject(String(data))

                                    SharedPreferencesState.init(this@NetworkService)
                                    SharedPreferencesState.addPropertyString(
                                        "tid",
                                        jsonObject.getString("tid")
                                    )

                                    try {
                                        if (jsonObject.getString("namegbr") !=
                                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "")
                                        ) {
                                            SharedPreferencesState.addPropertyString(
                                                "namegbr",
                                                jsonObject.getString("namegbr")
                                            )
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    try {
                                        if (jsonObject.getString("call") !=
                                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("call", "")
                                        ) {
                                            SharedPreferencesState.addPropertyString(
                                                "call",
                                                jsonObject.getString("call")
                                            )
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else
                                Log.d("Authorization", "failed")
                        }
                    }
                }
                try{
                    intent = Intent(StartActivity.BROADCAST_ACTION)
                    intent.putExtra("regok","ok")
                    sendBroadcast(intent)
                }catch (e:Exception){
                    e.printStackTrace()
                }
                try{
                    intent = Intent(ObjectActivity.BROADCAST_ACTION)
                    intent.putExtra("regok","ok")
                    sendBroadcast(intent)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            "notalarm"->{
                intent = Intent(StartActivity.BROADCAST_ACTION)
                intent.putExtra("notAlarm",true)
                sendBroadcast(intent)
            }
        }
    }

    override fun connectionLost() {
        var intent:Intent? = null

        onAirPackets.removeAll { true }
        incomingTransmissions.clear()
        outcomingTransmissions.clear()

        when{
            LoginActivity.Alive ->{
                intent = Intent(LoginActivity.BROADCAST_ACTION)
            }
            StartActivity.Alive ->{
                intent = Intent(StartActivity.BROADCAST_ACTION)
            }
            ObjectActivity.Alive ->{
                intent = Intent(ObjectActivity.BROADCAST_ACTION)

            }
        }
        intent?.putExtra("connectionLost",true)
        try{
            sendBroadcast(intent)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
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

    companion object {
        var appAlive = true
        var currentLocation: Location? = null
        lateinit var socket: DatagramSocket
        val packetsToSend = PriorityQueue<Packet>()
        var outcomingTransmissions = LongSparseArray<OutcomingTransmission>()
        var incomingTransmissions = LongSparseArray<IncomingTransmission>()
    }

    var channelId:String? = null
    private fun startForeground() {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
         channelId= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        val channelId = "101"
        val channelName = "My Background Service"
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
        } else {
            startService()
        }

        return START_STICKY
    }

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

    private fun startService() {

        sendLoop()

        receiverLoop()

        connectionLoop()
    }

    fun request(data: ByteArray, sessionID: String?, responseHandler: ResponseHandler) {
        this.createPacketOnQueue(
            data = data,
            sessionID = sessionID,
            contentType = binary,
            isWaitingForResponse = true,
            responseHandler = responseHandler
        )
    }

    fun request(message: String, sessionID: String?, responseHandler: ResponseHandler) {
        val data = message.toByteArray()
        this.createPacketOnQueue(
            data = data,
            sessionID = sessionID,
            contentType = string,
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
            contentType = string,
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
            contentType = binary,
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
            while (appAlive) {
                if (lastRequestTime + 5000 <= System.currentTimeMillis()) {
                    outcommingMessagesCount += 1
                    val messageNumber = outcommingMessagesCount
                    val sessionID = getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")
                    val packet = ConnectionPacket(messageNumber = messageNumber, sessionID = sessionID)
                    packetsToSend.enqueue(packet, 3)
                }
                sleep(4000)
            }
        }
    }

    @Synchronized private fun sendLoop() {
        thread {
            while (appAlive) {
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
                    }catch (e:Exception){
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
                }catch (e:Exception){
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

                            if (packet!!.headers!!.contentType == string ||
                                packet.headers!!.contentType == binary
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
            while (appAlive) {
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
                    connectionLost()
                    continue
                }

                connected = true
                lastResponseTime = System.currentTimeMillis()

                val packet = PacketUtils.decode(receiverPacket)

                when (packet.headers!!.contentType) {
                    acknowledgement -> {
                        handleAcknowledgmentPacket(packet as AcknowledgmentPacket)
                    }
                    connection -> {
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
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }
        }
    }

    private fun handleDataPacket(packet: DataPacket) {
        val acknowledgment = AcknowledgmentPacket(packet = packet)
        packetsToSend.enqueue(item = acknowledgment, priority = 2)

        val messageNumber = packet.headers!!.messageNumber

        if (messageNumber> this.incommingMessagesCount) {
            incommingMessagesCount = messageNumber
        }

        if (incomingTransmissions[messageNumber] == null) {
            val handler: ResponseHandler

            when (packet.headers!!.contentType) {
                string -> {

                    handler = handler@{ success: Boolean, data: ByteArray? ->
                        if (!success)
                            return@handler
                        if (data != null) {
                            messageReceived(String(data))
                        }
                    }
                }
                binary -> {
                    handler = handler@{ succes: Boolean, data: ByteArray? ->
                        if (succes) {
                            if (data != null)
                                messageReceived(data)
                        } else {
                            return@handler
                        }
                    }
                }
                acknowledgement -> TODO()
                connection -> TODO()
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

    override fun onDestroy() {
        super.onDestroy()

        Log.d("OnDestroy","Destroy service")

        stopForeground(true)
    }
}