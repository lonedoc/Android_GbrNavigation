package ru.rubeg38.rubegprotocol

enum class ContentType(val code: Byte) {
    CONNECTION(0xFE.toByte()),
    ACKNOWLEDGEMENT(0xFF.toByte()),
    STRING(0.toByte()),
    BINARY(1.toByte());
}