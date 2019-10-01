package ru.rubeg38.rubegprotocol

import java.util.concurrent.Semaphore

class SynchronizedArray<T> {
    private var arr: ArrayList<T>
    private val semaphore: Semaphore

    constructor() {
        this.arr = ArrayList()
        this.semaphore = Semaphore(1)
    }

    operator fun get(i: Int): T {
        this.semaphore.acquire()

        val item= this.arr[i]

        this.semaphore.release()

        return item
    }

    fun add(item: T) {
        this.semaphore.acquire()

        this.arr.add(item)

        this.semaphore.release()
    }

    fun removeAt(index: Int) {
        this.semaphore.acquire()

        this.arr.removeAt(index)

        this.semaphore.release()
    }

    fun count(): Int {
        this.semaphore.acquire()

        var count = this.arr.count()

        this.semaphore.release()

        return count
    }

    fun filter(predicate: (T) -> Boolean): List<T> {
        this.semaphore.acquire()

        var filtered = this.arr.filter(predicate)

        this.semaphore.release()

        return filtered
    }

    fun removeAll(predicate: (T) -> Boolean) {
        this.semaphore.acquire()

        this.arr.removeAll(predicate)

        this.semaphore.release()
    }

    fun copyItems(): ArrayList<T> {
        var copy: ArrayList<T>

        this.semaphore.acquire()

        copy = ArrayList(this.arr)

        this.semaphore.release()

        return copy
    }
}