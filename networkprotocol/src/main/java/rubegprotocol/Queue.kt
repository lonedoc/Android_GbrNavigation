package ru.rubeg38.rubegprotocol

import java.lang.IndexOutOfBoundsException
import java.util.concurrent.Semaphore

class Queue<T> {
    private val array: ArrayList<T>
    private val semaphore: Semaphore

    constructor() {
        this.array = ArrayList()
        this.semaphore = Semaphore(1)
    }

    fun enqueue(item: T) {
        this.semaphore.acquire()

        this.array.add(item)

        this.semaphore.release()
    }

    fun dequeue(): T? {
        var item: T? = null

        this.semaphore.acquire()

        if (this.array.count() > 0)
            item = this.array.removeAt(0)

        this.semaphore.release()

        return item
    }

    fun clear() {
        this.semaphore.acquire()

        this.array.clear()

        this.semaphore.release()
    }
}