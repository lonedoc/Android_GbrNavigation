package kobramob.rubeg38.ru.networkprotocol

interface RubegProtocolDelegate {
    var token: String?
    fun messageReceived(message: ByteArray)
    fun messageReceived(message: String)
    fun connectionLost()
}
