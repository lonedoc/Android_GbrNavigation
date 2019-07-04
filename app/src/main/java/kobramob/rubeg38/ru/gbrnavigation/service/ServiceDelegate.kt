package kobramob.rubeg38.ru.gbrnavigation.service

class ServiceDelegate() : NetworkServiceDelegate {
    override var sessionID: String
        get() = this.sessionID
        set(value) {}

    override fun messageReceived(message: ByteArray) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun messageReceived(message: String) {

        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun connectionLost() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    private val networkService: NetworkService = NetworkService()
}