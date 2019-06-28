package kobramob.rubeg38.ru.gbrnavigation.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.util.LongSparseArray
import kobramob.rubeg38.ru.gbrnavigation.R
import java.lang.Exception
import java.lang.Thread.sleep
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList

class NetworkService : Service(), LocationListener {
    val time: Time? = null

    private val SLEEP_INTERVAL: Int = 100_000
    private val CONNECTION_SYNC_INTERVAL = 10
    private val MAX_PACKETS_COUNT = 20
    private val ATTEMPTS_COUNT = 3

    private var outcommingMessagesCount: Long = 0
    private var incommingMessagesCount: Long = 0

    private val packetsToSend = PriorityQueue<Packet>()

    private var onAirPackets = ArrayList<Triple<Packet, Time, Int>>()
    private var messagesTransmissionInfo = LongSparseArray<Triple<BooleanArray?, Boolean, ResultHandler>>()
    private var messagesReceivingInfo = LongSparseArray<Triple<BooleanArray?, ByteBuffer?, ResultHandler>>()
    lateinit var ip: String
    var port: Int = 9010

    companion object {
        var currentLocation: Location? = null
    }

    private val socket = DatagramSocket(null)

    override fun onCreate() {
        super.onCreate()
        getLocation()
        startForeground()
    }

    fun initSocket(ip: String, port: Int) {
        this.port = port
        this.ip = ip
        socket.reuseAddress = true
        socket.bind(InetSocketAddress(PollingServer.socket.localAddress, PollingServer.socket.localPort))
        socket.connect(InetSocketAddress(ip, port))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val connectionLoop = Runnable {
            // connectionLoop()
        }; Thread(connectionLoop).start()

        val sendLoop = Runnable {
            sendLoop()
        }; Thread(sendLoop).start()

        val receiverLoop = Runnable {
            receiverLoop()
        }; Thread(receiverLoop).start()
        return START_STICKY
    }

    private fun connectionLoop() {
        while (true) {
            Log.d("ConnectionLoop", "Work")
            this.packetsToSend.enqueue(ConnectionPacket(), 3)
            sleep(10000)
        }
    }

    private fun sendLoop() {
        // Retransmit
        while (true) {
            for (i in 0 until this.onAirPackets.count()) {
                Log.d("SendLoop", "Work")

                if (this.onAirPackets[i].second.time <= time!!.time) {
                    val packet = this.onAirPackets[i].first
                    val time = this.onAirPackets[i].second
                    val count = this.onAirPackets[i].third
                    this.onAirPackets.removeAt(i)

                    this.onAirPackets.add(i, Triple(packet, time, count + 1))
                    if (this.onAirPackets[i].third> this.MAX_PACKETS_COUNT) {
                        Log.d("SendLoop", "PacketFailed")
                        continue
                    }
                    // debug
                    Log.d("SendLoop", "PacketRetransmitted")
                    val packet1 = this.onAirPackets[i].first
                    val time1 = Time(time.time + 10)
                    val count1 = this.onAirPackets[i].third
                    this.onAirPackets.removeAt(i)
                    Log.d("SendLoop", "Time $time1")
                    this.onAirPackets.add(i, Triple(packet1, time1, count1))

                    try {
                        this.socket.send(onAirPackets[i].first.encode(ip, port))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            // remove packet

            sleep(100)
            // add new packet
            try {
                if (this.onAirPackets.count() <this.MAX_PACKETS_COUNT) {
                    val packet: Packet?
                    if (packetsToSend.dequeue() != null) {
                        packet = packetsToSend.dequeue()
                        println("Пакет есть")
                        if (packet!!.type == PacketType.data) {
                            val time10sec = Time(time!!.time + 10)
                            this.onAirPackets.add(Triple(packet, time10sec, 1))
                            Log.d("SendLoop", "DataPacket")
                        }
                        if (packet.type == PacketType.connection) {
                            Log.d("SendLoop", "ConnectionPacket")
                            onAirPackets.removeAt(0)
                            packetsToSend.remove()
                        }
                        Log.d("SendLoop", "PacketWritten")
                        try {
                            this.socket.send(packet.encode(ip, port))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Remove failed packet
    }

    private fun receiverLoop() {
    }

    fun send(data: ByteArray, sessionID: String?, isWaitingForResponse: Boolean, resultHandler: ResultHandler) {
        // messageNumber
        val messageNumber = this.outcommingMessagesCount + 1

        // Count of packet
        var packetCount = data.count() / 1000

        if (data.count() % 1000 != 0) {
            packetCount += 1
        }

        // Transmission control
        val booleanArray = BooleanArray(packetCount)
        for (i in 0 until booleanArray.size)
            booleanArray[i] = false

        messagesTransmissionInfo.put(messageNumber, Triple(booleanArray, isWaitingForResponse, resultHandler))

        // CreatePacket
        var packetNumber: Int = 1

        var leftBound = 0
        while (leftBound <data.count()) {
            val rightBound = if (leftBound + 1000 < data.count())
                leftBound + 1000
            else
                data.count()

            val chunk = data.slice(IntRange(leftBound, rightBound - 1))

            val packet = DataPacket(
                data = chunk.toByteArray(),
                sessionID = sessionID,
                contentType = ContentType.string,
                messageNumber = messageNumber,
                messageSize = data.count(),
                shift = leftBound,
                packetCount = packetCount,
                packetNumber = packetNumber
            )

            this.packetsToSend.enqueue(packet)

            packetNumber += 1
            leftBound += 1000
        }
        this.outcommingMessagesCount += 1
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
            } catch (e: Exception) {}
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            } catch (e: Exception) {}
        } catch (e: Exception) { e.printStackTrace() }
    }
    private fun startForeground() {

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                "101"
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification =
            notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(channelId.toInt(), notification)
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
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onLocationChanged(location: Location?) {
        currentLocation = location
    }
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
    override fun onProviderEnabled(provider: String?) {
    }
    override fun onProviderDisabled(provider: String?) {
    }
}