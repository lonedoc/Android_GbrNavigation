package kobramob.rubeg38.ru.gbrnavigation.service

interface NetworkServiceDelegate {
    var sessionID: String
    fun messageReceived(message: ByteArray)
    fun messageReceived(message: String)
    fun connectionLost()
}