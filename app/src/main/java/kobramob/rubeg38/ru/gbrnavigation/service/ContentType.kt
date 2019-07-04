package kobramob.rubeg38.ru.gbrnavigation.service

enum class ContentType(val contentType: Byte) {
    acknowledgement(0xFF.toByte()),
    connection(0xFE.toByte()),
    string(0x00.toByte()),
    binary(0x01.toByte())
}