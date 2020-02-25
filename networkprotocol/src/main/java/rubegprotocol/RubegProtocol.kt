package rubegprotocol

import android.util.Log
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.*
import ru.rubeg38.rubegprotocol.PriorityQueue
import ru.rubeg38.rubegprotocol.Queue
import java.io.IOException
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class RubegProtocol {
    companion object {
        private const val PACKET_SIZE = 962
        private const val CONNECTION_DROP_INTERVAL = 20_000
        private const val SYNC_INTERVAL = 3000
        private const val RETRANSMIT_INTERVAL = 10_000
        private const val SLEEP_INTERVAL: Long = 100
        private const val MAX_ATTEMPTS_COUNT = 3
        private const val CONGESTION_WINDOW_SIZE = 32
        val sharedInstance: RubegProtocol by lazy {
            RubegProtocol()
        }
    }

    private var started = false
    private var connected = false

    private var lastRequestTime: Long = 0
    private var lastResponseTime: Long = 0

    private var hosts = ArrayList<InetSocketAddress>()
    private var currentHostIndex = 0

    private var socket: DatagramChannel

    private var connectionWatchers = ArrayList<ConnectionWatcher?>()
    private var textMessageWatchers = CopyOnWriteArrayList<TextMessageWatcher?>()
    private var binaryMessageWatchers = CopyOnWriteArrayList<BinaryMessageWatcher?>()

    private var outcomingMessagesCount: Long = 0
    private var incomingMessagesCount: Long = 0

    private var outcomingTransmissions = HashMap<Long, OutcomingTransmission>()
    private var incomingTransmissions = HashMap<Long, IncomingTransmission>()

    private val packetsQueue = PriorityQueue<Packet>()
    private val acksQueue = Queue<AcknowledgementPacket>()
    private var congestionWindow = ArrayList<Transmission>()

//    private val readLoopSemaphore = Semaphore(1, true)
    private val sendLoopSemaphore = Semaphore(1, true)

    private constructor() {
        socket = DatagramChannel.open()
        socket.configureBlocking(false)
    }

    fun subscribe(watcher: ConnectionWatcher): () -> Unit {
        var i = 0

        while (i < this.connectionWatchers.count()) {
            if (this.connectionWatchers[i] == null) {
                this.connectionWatchers[i] = watcher

                return { this.connectionWatchers[i] = null }
            }

            i++
        }

        this.connectionWatchers.add(watcher)

        return { this.connectionWatchers[i] = null }
    }

    fun subscribe(watcher: TextMessageWatcher): () -> Unit {
        var i = 0


        while (i < this.textMessageWatchers.count()) {
            Log.d("Subscribe","$textMessageWatchers")
            if (this.textMessageWatchers[i] == null) {
                this.textMessageWatchers[i] = watcher

                return {
                    this.textMessageWatchers[i] = null }
            }

            i++
        }

        this.textMessageWatchers.add(watcher)

        return {
            this.textMessageWatchers[i] = null }
    }

    fun subscribe(watcher: BinaryMessageWatcher): () -> Unit {
        var i = 0

        while (i < this.binaryMessageWatchers.count()) {
            if (this.binaryMessageWatchers[i] == null) {
                this.binaryMessageWatchers[i] = watcher

                return { this.binaryMessageWatchers[i] = null }
            }

            i++
        }

        this.binaryMessageWatchers.add(watcher)

        return { this.binaryMessageWatchers[i] = null }
    }

    fun subscribe(watcher: MessageWatcher): () -> Unit {
        return {
            this.subscribe(watcher as TextMessageWatcher)()
            this.subscribe(watcher as BinaryMessageWatcher)()
        }
    }

    val isStarted: Boolean
        get() = started

    val isConnected: Boolean
        get() = connected && !token.isNullOrBlank()

    var token: String? = null

    fun send(message: String, onComplete: (Boolean) -> Unit) {
        send(message.toByteArray(), ContentType.STRING, onComplete)
    }

    fun send(message: ByteArray, onComplete: (Boolean) -> Unit) {
        send(message, ContentType.BINARY, onComplete)
    }

    private fun send(data: ByteArray, contentType: ContentType, onComplete: (Boolean) -> Unit) {
        thread {
            outcomingMessagesCount++

            val messageNumber = outcomingMessagesCount

            var packetsCount = data.count() / PACKET_SIZE

            if (data.count() % PACKET_SIZE != 0)
                packetsCount++

            outcomingTransmissions[messageNumber] = OutcomingTransmission(onComplete, packetsCount)

            var packetNumber = 1

            var leftBound = 0
            while (leftBound < data.count()) {
                val rightBound = if (leftBound + PACKET_SIZE < data.count()) leftBound + PACKET_SIZE else data.count()

                val chunk = data.sliceArray(leftBound until rightBound)

                val packet = DataPacket(
                    chunk,
                    token,
                    contentType,
                    messageNumber,
                    data.count(),
                    leftBound,
                    packetsCount,
                    packetNumber
                )

                packetsQueue.enqueue(packet, Priority.MEDIUM)

                packetNumber++
                leftBound += PACKET_SIZE
            }
        }
    }

    fun configure(addresses: ArrayList<String>, port: Int) {
        check(!started) { "It's required to stop protocol before configuring" }
        require(addresses.count() != 0) { "At least one ip address required" }

        hosts = ArrayList(addresses.map { InetSocketAddress(it, port) })
        currentHostIndex = 0
    }

    fun start() {
        started = true

        sendLoop()

        readLoop()

        lastResponseTime = System.currentTimeMillis()
    }

    fun stop() {
        started = false
    }

    private fun reset() {
        // Debug
        println("Protocol: reset")

//        readLoopSemaphore.acquire()
        sendLoopSemaphore.acquire()

        println("Protocol reset: Lock")

        connected = false

        congestionWindow.clear()
        packetsQueue.clear()
        acksQueue.clear()

        outcomingTransmissions.forEach { it.value.onComplete(false) }

        incomingTransmissions.clear()
        outcomingTransmissions.clear()

        incomingMessagesCount = 0
        outcomingMessagesCount = 0

        token = null

//        readLoopSemaphore.release()
        sendLoopSemaphore.release()

        println("Protocol reset: Unlock")
    }

    private fun readLoop() {
        thread {
            while (started) {
//                readLoopSemaphore.acquire()

                if (lastResponseTime + CONNECTION_DROP_INTERVAL <= System.currentTimeMillis()) {
                    lastResponseTime = System.currentTimeMillis()

                    reset()

                    connectionWatchers.forEach { it?.onConnectionLost() }

                    currentHostIndex++
                }

                var buffer = ByteBuffer.allocate(1536)

                try {
                    socket.receive(buffer)

                    buffer.flip()

                    if (!buffer.hasRemaining()) {
//                        readLoopSemaphore.release()
                        continue
                    }
                } catch (ex: IOException) {
//                    readLoopSemaphore.release()
                    continue
                }

                if (!connected)
                    connectionWatchers.forEach { it?.onConnectionEstablished() }

                connected = true
                lastResponseTime = System.currentTimeMillis()

                val packet:Packet = PacketUtils.decode(buffer.array()) ?: continue

                // Debug
                println("<- { content type: ${packet.headers.contentType}, message number: ${packet.headers.messageNumber}, packet number: ${packet.headers.packetNumber} }")

                when (packet.headers.contentType) {
                    ContentType.ACKNOWLEDGEMENT -> {
                        handleAcknowledgement(packet as AcknowledgementPacket)
                    }
                    ContentType.BINARY, ContentType.STRING -> {
                        handleData(packet as DataPacket)
                    }
                    else -> {

                    }
                }

                incomingTransmissions = HashMap(incomingTransmissions.filter { !it.value.failed })

//                readLoopSemaphore.release()
            }

            reset()
        }
    }

    private fun sendLoop() {
        thread {
            while (started) {
                if (sendLoopSemaphore.availablePermits() == 0)
                    sendLoopSemaphore.release()

                sendLoopSemaphore.acquire()

                // Handle acknowledgements
                var ack = acksQueue.dequeue()

                while (ack != null) {
                    congestionWindow.removeAll { samePacketSignature(ack!!, it.packet)}

                    ack = acksQueue.dequeue()
                }

                // Retransmit packets
                congestionWindow.forEach { transmission -> // TODO: Fix the concurrent modification problem
                    if (transmission.lastAttemptTime + RETRANSMIT_INTERVAL <= System.currentTimeMillis()) {
                        if (transmission.attemptsCount < MAX_ATTEMPTS_COUNT) {
                            try {
                                sendPacket(transmission.packet)
                            } catch (ex: IOException) {
                                ex.printStackTrace()
                            }

                            transmission.lastAttemptTime = System.currentTimeMillis()

                            // Debug
                            println("-> -> { attempt: ${transmission.attemptsCount}, content type: ${transmission.packet.headers.contentType}, message number: ${transmission.packet.headers.messageNumber}, packet number: ${transmission.packet.headers.packetNumber} }")
                        }

                        transmission.attemptsCount++
                    }
                }

                // Remove failed
                val failedTransmissions = congestionWindow.filter { it.attemptsCount > MAX_ATTEMPTS_COUNT }

                failedTransmissions.forEach { transmission ->
                    val messageNumber = transmission.packet.headers.messageNumber

                    packetsQueue.removeAll { it.headers.messageNumber == messageNumber }

                    outcomingTransmissions[messageNumber]?.onComplete?.invoke(false)
                    outcomingTransmissions.remove(messageNumber)
                }

                congestionWindow.removeAll { it.attemptsCount > MAX_ATTEMPTS_COUNT }

                // Send
                val congestionWindowIsFull = congestionWindow.count() >= CONGESTION_WINDOW_SIZE
                val packetsQueueIsEmpty = packetsQueue.count() == 0

                if (!packetsQueueIsEmpty && !congestionWindowIsFull) {
                    val packet = packetsQueue.dequeue()

                    if (packet != null) {
                        if (packet.headers.contentType in arrayListOf(
                                ContentType.STRING,
                                ContentType.BINARY
                            )) {
                            congestionWindow.add(
                                Transmission(
                                packet,
                                System.currentTimeMillis(),
                                1
                            )
                            )
                        }

                        try {
                            sendPacket(packet)
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }

                        sendLoopSemaphore.release()
                        continue
                    }
                }

                // Maintain connection
                val syncTimeHasCome = lastRequestTime + SYNC_INTERVAL <= System.currentTimeMillis()

                if (syncTimeHasCome) {
                    if (isConnected) {
                        val connectionPacket = ConnectionPacket(token!!)

                        try {
                            sendPacket(connectionPacket)
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }
                    }
                }

                if (packetsQueueIsEmpty)
                    sleep(SLEEP_INTERVAL)

                sendLoopSemaphore.release()
            }

            reset()
        }
    }

    private fun sendPacket(packet: Packet) {
        val buffer = ByteBuffer.wrap(packet.encode())
        val host = hosts[currentHostIndex % hosts.count()]

        if(currentHostIndex>hosts.count() && incomingMessagesCount==0.toLong()){
            val jsonObject = JSONObject()
            jsonObject.put("\$c$","ServerNotResponse")
            textMessageWatchers.forEach { it?.onTextMessageReceived(jsonObject.toString()) }
        }

        socket.send(buffer, host)

        lastRequestTime = System.currentTimeMillis()

        // Debug
        println("-> { content type: ${packet.headers.contentType}, message number: ${packet.headers.messageNumber}, packet number: ${packet.headers.packetNumber} }")
    }

    private fun samePacketSignature(packetA: Packet, packetB: Packet): Boolean {
        val sameMessageNumber = packetA.headers.messageNumber == packetB.headers.messageNumber
        val samePacketsCount = packetA.headers.packetsCount == packetB.headers.packetsCount
        val samePacketNumber = packetA.headers.packetNumber == packetB.headers.packetNumber

        return sameMessageNumber && samePacketsCount && samePacketNumber
    }

    private fun handleData(packet: DataPacket) {

        val messageNumber = packet.headers.messageNumber

        if(packet.headers.sessionId != null)
        Log.d("handleData",packet.headers.sessionId)

        val acknowledgement = AcknowledgementPacket(packet)

        packetsQueue.enqueue(acknowledgement, Priority.HIGH)

        if (!incomingTransmissions.containsKey(messageNumber) && messageNumber <= incomingMessagesCount && messageNumber != 0L)
            return



        if (messageNumber > incomingMessagesCount)
            incomingMessagesCount = messageNumber



        if (!incomingTransmissions.containsKey(messageNumber))
            incomingTransmissions[messageNumber] = IncomingTransmission()

        val transmission = incomingTransmissions[messageNumber]

        if (transmission != null) {
            transmission.addPacket(packet)

            if (transmission.done) {
                when (packet.headers.contentType) {
                    ContentType.STRING -> {
                        val message = String(transmission.message!!)
                        textMessageWatchers.forEach { it?.onTextMessageReceived(message) }
                    }
                    ContentType.BINARY -> {
                        val message = transmission.message!!
                        binaryMessageWatchers.forEach { it?.onBinaryMessageReceived(message) }
                    }
                    ContentType.CONNECTION -> TODO()
                    ContentType.ACKNOWLEDGEMENT -> TODO()
                }

                incomingTransmissions.remove(messageNumber)
            }
        }
    }

    private fun handleAcknowledgement(packet: AcknowledgementPacket) {
        acksQueue.enqueue(packet)

        // Update transmission info
        val messageNumber = packet.headers.messageNumber
        val packetNumber = packet.headers.packetNumber

        val transmission = outcomingTransmissions[messageNumber]

        if (transmission != null) {
            transmission.addAcknowledgement(packetNumber - 1)

            if (transmission.done) {
                transmission.onComplete(true)

                outcomingTransmissions.remove(messageNumber)
            }
        }
    }
}