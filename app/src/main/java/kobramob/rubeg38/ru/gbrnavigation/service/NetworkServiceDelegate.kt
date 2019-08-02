package kobramob.rubeg38.ru.gbrnavigation.service

interface NetworkServiceDelegate {
    var sessionId: String?
    fun messageReceived(message: ByteArray)
    fun messageReceived(message: String)
    fun connectionLost()
}