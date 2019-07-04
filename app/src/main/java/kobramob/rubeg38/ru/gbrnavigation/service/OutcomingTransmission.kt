package kobramob.rubeg38.ru.gbrnavigation.service

class OutcomingTransmission {
    private lateinit var acknowledgements: BooleanArray

    var isResponseExpected: Boolean
        private set

    lateinit var responseHandler: ResponseHandler
        private set

    constructor(packetCount: Int, isResponseExpected: Boolean, responseHandler: ResponseHandler) {
        this.acknowledgements = BooleanArray(packetCount)
        this.isResponseExpected = isResponseExpected
        this.responseHandler = responseHandler
    }

    var done: () -> Boolean = {
        acknowledgements.all { it }
    }
    fun addAcknowledgements(packetNumber: Int) {
        acknowledgements[packetNumber] = true
    }
}