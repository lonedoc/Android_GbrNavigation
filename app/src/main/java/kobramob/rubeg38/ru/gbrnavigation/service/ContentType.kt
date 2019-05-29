package kobramob.rubeg38.ru.gbrnavigation.service

enum class ContentType(val contentType: Byte) {
    empty(0xFF.toByte()),
    string(0x00.toByte()),
    binary(0x01.toByte())
}