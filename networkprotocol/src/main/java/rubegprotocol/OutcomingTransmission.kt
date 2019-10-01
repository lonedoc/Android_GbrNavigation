package ru.rubeg38.rubegprotocol

class OutcomingTransmission(val onComplete: (Boolean) -> Unit, packetsCount: Int) {
    private val acknowledgements = BooleanArray(packetsCount)

    val done: Boolean
        get() = acknowledgements.all { it }

    fun addAcknowledgement(packetNumber: Int) {
        acknowledgements[packetNumber] = true
    }
}