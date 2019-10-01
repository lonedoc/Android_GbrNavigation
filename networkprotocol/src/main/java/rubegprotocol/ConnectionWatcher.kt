package ru.rubeg38.rubegprotocol

interface ConnectionWatcher {
    fun onConnectionLost()
    fun onConnectionEstablished()
}