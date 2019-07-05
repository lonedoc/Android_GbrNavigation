package kobramob.rubeg38.ru.gbrnavigation.service

interface NetworkServiceDelegate {
    fun messageReceived(message: ByteArray)
    fun messageReceived(message: String)
    fun connectionLost()
}