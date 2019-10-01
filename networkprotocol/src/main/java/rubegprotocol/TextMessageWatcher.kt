package ru.rubeg38.rubegprotocol

interface TextMessageWatcher {
    fun onTextMessageReceived(message: String)
}