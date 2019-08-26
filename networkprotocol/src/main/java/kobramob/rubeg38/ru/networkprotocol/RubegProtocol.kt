package kobramob.rubeg38.ru.networkprotocol

import android.util.Log
import java.io.IOException
import java.lang.Thread.sleep
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class RubegProtocol {
    private val PACKET_SIZE = 962
    private val SLEEP_INTERVAL: Long = 100
    private val CONNECTION_SYNC_INTERVAL = 5000
    private val MAX_PACKETS_COUNT = 32
    private val ATTEMPTS_COUNT = 3

    private val semaphore: Semaphore

    private val hosts: ArrayList<InetSocketAddress>
    private var currentHostIndex: Int

    private val socket: DatagramSocket
    private val channel: DatagramChannel

    private var lastRequestTime: Long
    private var lastResponseTime: Long

    var connected: Boolean

    private var outcomingMessagesCount: Long
    private var incomingMessagesCount: Long

    private val packetsToSend: PriorityQueue<Packet>
    private var onAirPackets: CopyOnWriteArrayList<RetransmissionInfo>
    private var unhandledAcks: Queue<AcknowledgementPacket>
    private var failedPackets:ArrayList<Long> = ArrayList()

    private var outcomingTransmissions: HashMap<Long, OutcomingTransmission>
    private var incomingTransmissions: HashMap<Long, IncomingTransmission>

    var delegate: RubegProtocolDelegate?

    var percent :Int = 0

    var protocolAlive:Boolean = false

    constructor(ips: ArrayList<String>, port: Int) {
        require(ips.count() != 0) { "At least one ip address required" }

        this.hosts = ArrayList()

        for (ip in ips) {
            this.hosts.add(InetSocketAddress(ip, port))
        }

        this.currentHostIndex = 0

        this.socket = DatagramSocket(null)
        this.socket.reuseAddress = false
        this.socket.soTimeout = 1

        this.lastRequestTime = System.currentTimeMillis()
        this.lastResponseTime = System.currentTimeMillis()

        this.connected = false

        this.outcomingMessagesCount = 0
        this.incomingMessagesCount = 0

        this.packetsToSend = PriorityQueue()
        this.onAirPackets = CopyOnWriteArrayList()
        this.unhandledAcks = Queue()

        this.outcomingTransmissions = HashMap()
        this.incomingTransmissions = HashMap()

        this.delegate = null

        this.semaphore = Semaphore(1)

        this.channel = DatagramChannel.open()
        this.channel.configureBlocking(false)
    }

    val isConnected: Boolean
        get() {
            return this.connected && this.delegate?.sessionId != null
        }

    fun request(message: String, responseHandler: ResponseHandler) {
        this.send(
            message.toByteArray(),
            ContentType.STRING,
            this.delegate?.sessionId,
            true,
            responseHandler
        )
    }

    fun request(data: ByteArray, responseHandler: ResponseHandler) {
        this.send(
            data,
            ContentType.BINARY,
            this.delegate?.sessionId,
            true,
            responseHandler
        )
    }

    fun send(message: String, resultHandler: ResultHandler) {
        this.send(
            message.toByteArray(),
            ContentType.STRING,
            this.delegate?.sessionId,
            false
        ) { success, _ -> resultHandler(success) }
    }

    fun send(data: ByteArray, resultHandler: ResultHandler) {
        this.send(
            data,
            ContentType.BINARY,
            this.delegate?.sessionId,
            false
        ) { success, _ -> resultHandler(success) }
    }

    private fun send(data: ByteArray, contentType: ContentType, sessionId: String?, isResponseExpected: Boolean, responseHandler: ResponseHandler) {
        thread {
            this.outcomingMessagesCount++

            val messageNumber = this.outcomingMessagesCount

            // Count of packets
            var packetsCount = data.count() / PACKET_SIZE

            if (data.count() % PACKET_SIZE != 0) {
                packetsCount++
            }

            // Transmission control
            this.outcomingTransmissions[messageNumber] = OutcomingTransmission(
                    packetsCount,
                    isResponseExpected,
                    responseHandler
            )

            // Create packets
            var packetNumber = 1

            var leftBound = 0
            while (leftBound < data.count()) {

                if(failedPackets.contains(messageNumber)){
                    failedPackets.remove(messageNumber)
                    return@thread
                }

                val rightBound = if (leftBound + PACKET_SIZE < data.count()) leftBound + PACKET_SIZE else data.count()

                val chunk = data.sliceArray(leftBound until rightBound)

                val packet = DataPacket(
                        chunk,
                        sessionId,
                        contentType,
                        messageNumber,
                        data.count(),
                        leftBound,
                        packetsCount,
                        packetNumber
                )

                this.packetsToSend.enqueue(packet, Priority.MEDIUM)

                packetNumber++
                leftBound += PACKET_SIZE
            }
        }
    }

    fun start() {
        protocolAlive = true
        this.startSendLoop()
        this.startReadLoop()
    }

    fun stop(){
        protocolAlive = false

        this.onAirPackets.forEach { onAirPacket ->
            val messageNumber = onAirPacket.packet.headers.messageNumber

            this.outcomingTransmissions[messageNumber]?.responseHandler?.invoke(false, null)
            this.outcomingTransmissions.remove(messageNumber)
        }

        this.lastResponseTime = System.currentTimeMillis()

        this.outcomingMessagesCount = 0
        this.incomingMessagesCount = 0

        this.connected = false

        this.packetsToSend.removeAll { true }

        this.onAirPackets.clear()
        this.incomingTransmissions.clear()
        this.outcomingTransmissions.clear()


    }

    private fun startReadLoop() {
        thread {
            while (protocolAlive) {

                if (this.lastResponseTime + 10000 <= System.currentTimeMillis()) {

                    this.lastResponseTime = System.currentTimeMillis()

                    this.connected = false

                    this.outcomingMessagesCount = 0
                    this.incomingMessagesCount = 0

                    this.onAirPackets.clear()

                    this.packetsToSend.removeAll { true }

                    if(incomingTransmissions.isNotEmpty())
                    this.incomingTransmissions.forEach { it.value.responseHandler(false, null) }

                    if(outcomingTransmissions.isNotEmpty())
                    this.outcomingTransmissions.forEach { it.value.responseHandler(false, null) }

                    this.incomingTransmissions.clear()
                    this.outcomingTransmissions.clear()



                    this.delegate?.connectionLost()

                    this.currentHostIndex++

                    this.delegate?.sessionId = null
                }

                val buffer = ByteBuffer.allocate(1536)

                try {
                    this.channel.receive(buffer)

                    buffer.flip()

                    if (!buffer.hasRemaining())
                        continue

                } catch (ex: IOException) {
                    continue
                }

                this.connected = true
                this.lastResponseTime = System.currentTimeMillis()

                val packet = PacketUtils.decode(buffer.array())

                // debug
                println("<- { content type: ${packet.headers.contentType},  message number: ${packet.headers.messageNumber}, packet number: ${packet.headers.packetNumber} }")

                if (packet.headers.contentType == ContentType.ACKNOWLEDGEMENT) {
                    handleAcknowledgementPacket(packet as AcknowledgementPacket)
                } else if (packet.headers.contentType == ContentType.CONNECTION) {
                    val messageNumber = packet.headers.messageNumber

                    if (messageNumber > this.incomingMessagesCount)
                        this.incomingMessagesCount = messageNumber
                } else {
                    handleDataPacket(packet as DataPacket)
                }

                // Handle failed messages
                val failedTransmissions = this.incomingTransmissions.filter { it.value.failed }

                failedTransmissions.forEach {
                    it.value.responseHandler(false, null)
                    this.incomingTransmissions.remove(it.key)


                }
            }
        }
    }

    private fun startSendLoop() {
        thread {
            while (protocolAlive) {
                // Handle acknowledgements
                var ack = this.unhandledAcks.dequeue()

                while (ack != null) {
                    this.onAirPackets.removeAll {
                        this.samePacketSignature(ack!!, it.packet)
                    }

                    ack = this.unhandledAcks.dequeue()
                }

                // Retransmit packets
                this.onAirPackets.forEach { info -> // TODO: Rename retransmission info
                    if (info.lastAttemptTime + 10000 <= System.currentTimeMillis()) {
                        if (info.attemptsCount < ATTEMPTS_COUNT) { // TODO: Rename to MAX_ATTEMPTS_COUNT
                            try {
                                this.sendPacket(info.packet)
                            } catch (ex: IOException) {
                                ex.printStackTrace()
                            }

                            info.lastAttemptTime = System.currentTimeMillis()

                            // debug
                            println("-> -> { attempt: ${info.attemptsCount}, content type: ${info.packet.headers.contentType}, message number: ${info.packet.headers.messageNumber}, packet number: ${info.packet.headers.packetNumber} }")
                        }

                        info.attemptsCount++
                    }
                }

                // Remove failed
                val failedPackets = this.onAirPackets.filter { it.attemptsCount > ATTEMPTS_COUNT }

                failedPackets.forEach { failedPacket ->
                    val messageNumber = failedPacket.packet.headers.messageNumber

                    this.packetsToSend.removeAll { it.headers.messageNumber == messageNumber }

                    this.outcomingTransmissions[messageNumber]?.responseHandler?.invoke(false, null)
                    this.outcomingTransmissions.remove(messageNumber)
                }

                this.onAirPackets.removeAll { it.attemptsCount > ATTEMPTS_COUNT }

                // Send
                val windowIsFull = this.onAirPackets.count() >= MAX_PACKETS_COUNT
                val nothingToSend = this.packetsToSend.count() == 0
                val syncTimeHasCome = this.lastRequestTime + 8900 <= System.currentTimeMillis() // 10s - (max RTT + interval)

                if (!nothingToSend && !windowIsFull) {
                    val packet = this.packetsToSend.dequeue()

                    if (packet != null) {
                        if (packet.headers.contentType == ContentType.STRING || packet.headers.contentType == ContentType.BINARY) {
                            this.onAirPackets.add(
                                RetransmissionInfo(
                                packet,
                                System.currentTimeMillis(),
                                1
                            )
                            )
                        }

                        try {
                            this.sendPacket(packet)
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }

                        continue
                    }
                }

                // Maintain connection
                if (syncTimeHasCome) {
                    val sessionId = this.delegate?.sessionId

                    if (this.isConnected) {
                        val connectionPacket = ConnectionPacket(sessionId!!)

                        try {
                            this.sendPacket(connectionPacket)
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }
                    }
                }

                if (nothingToSend)
                    sleep(100) // interval
            }
        }
    }

    private fun sendPacket(packet: Packet) {
        val buffer = ByteBuffer.wrap(packet.encode())

        val host = this.hosts[this.currentHostIndex % this.hosts.count()]

        this.channel.send(buffer, host)

        this.lastRequestTime = System.currentTimeMillis()

        // debug
        println("-> { content type: ${packet.headers.contentType}, message number: ${packet.headers.messageNumber}, packet number: ${packet.headers.packetNumber} }")
    }

    private fun samePacketSignature(packetA: Packet, packetB: Packet): Boolean {
        val sameMessageNumber = packetA.headers.messageNumber == packetB.headers.messageNumber
        val samePacketsCount = packetA.headers.packetsCount == packetB.headers.packetsCount
        val samePacketNumber = packetA.headers.packetNumber == packetB.headers.packetNumber

        return sameMessageNumber && samePacketsCount && samePacketNumber
    }

    private fun handleDataPacket(packet: DataPacket) {
        val messageNumber = packet.headers.messageNumber

        if (!this.incomingTransmissions.containsKey(messageNumber) && messageNumber < this.incomingMessagesCount) {
            return
        }

        val acknowledgement = AcknowledgementPacket(packet)

        this.packetsToSend.enqueue(acknowledgement, Priority.HIGH)

        if (messageNumber > this.incomingMessagesCount) {
            this.incomingMessagesCount = messageNumber
        }


        if (!this.incomingTransmissions.containsKey(messageNumber)) {

            val handler: ResponseHandler

            handler = if (packet.headers.contentType == ContentType.STRING) {
                h1@{ success, data ->
                    if (!success)
                        return@h1

                    val text = String(data!!)

                    this.delegate?.messageReceived(text)
                }
            } else {
                { success, data ->
                    if (success)
                        this.delegate?.messageReceived(data!!)
                }
            }

            this.incomingTransmissions[messageNumber] = IncomingTransmission(handler)
        }

        if (this.incomingTransmissions.containsKey(messageNumber)) {
            val transmission = this.incomingTransmissions[messageNumber]!!

            transmission.addPacket(packet)

            this.percent = transmission.percent

            Log.d("IncomingTransmission","Transmission: ${transmission.done}")
            if (transmission.done) {
                transmission.responseHandler(true, transmission.message)

                this.incomingTransmissions.remove(messageNumber)
                percent = 0
            }
        }
    }

    private fun handleAcknowledgementPacket(packet: AcknowledgementPacket) {
        this.unhandledAcks.enqueue(packet)

        val messageNumber = packet.headers.messageNumber
        val packetNumber = packet.headers.packetNumber

        // Update messages transmission info
        val transmission = this.outcomingTransmissions[messageNumber]

        if (transmission != null) {
            transmission.addAcknowledgement(packetNumber - 1)

            if (transmission.done) {
                if (transmission.isResponseExpected) {
                    val expectedNumber = this.incomingMessagesCount + 1

                    this.incomingTransmissions[expectedNumber] = IncomingTransmission(transmission.responseHandler)

                } else {
                    transmission.responseHandler(true, null)
                }

                this.outcomingTransmissions.remove(messageNumber)
            }
        }
    }
}
