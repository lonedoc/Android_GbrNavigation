package ru.rubeg38.rubegprotocol

interface BinaryMessageWatcher {
    fun onBinaryMessageReceived(message: ByteArray)
}