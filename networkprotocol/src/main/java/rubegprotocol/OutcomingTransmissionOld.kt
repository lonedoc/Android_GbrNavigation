package ru.rubeg38.rubegprotocol

class OutcomingTransmissionOld {
    private var acknowledgements: BooleanArray
    var isResponseExpected: Boolean
    var responseHandler: ResponseHandler

    constructor(packetsCount: Int, isResponseExpected: Boolean, responseHandler: ResponseHandler) {
        this.acknowledgements = BooleanArray(packetsCount)
        this.isResponseExpected = isResponseExpected
        this.responseHandler = responseHandler
    }

    val done: Boolean
    get()= this.acknowledgements.all { it }

    fun addAcknowledgement(packetNumber: Int) {
        this.acknowledgements[packetNumber] = true
    }
}
