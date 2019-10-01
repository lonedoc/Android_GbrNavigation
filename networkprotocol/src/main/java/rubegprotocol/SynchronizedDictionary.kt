package ru.rubeg38.rubegprotocol

import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.HashMap

class SynchronizedDictionary<K, V> {
    private var dict: HashMap<K, V>
    private val semaphore: Semaphore

    constructor() {
        this.dict = HashMap()
        this.semaphore = Semaphore(1)
    }

    operator fun get(key: K): V? {
        var value: V?

        this.semaphore.acquire()

        value = this.dict[key]

        this.semaphore.release()

        return value
    }

    operator fun set(key: K, value: V) {
        this.semaphore.acquire()

        this.dict[key] = value

        this.semaphore.release()
    }

    fun remove(key: K): V? {
        var value: V?

        this.semaphore.acquire()

        value = this.dict.remove(key)

        this.semaphore.release()

        return value
    }

    fun clear() {
        this.semaphore.acquire()

        this.dict.clear()

        this.semaphore.release()
    }

    fun filter(predicate: (Map.Entry<K, V>) -> Boolean): Map<K, V> {
        var map: Map<K, V>

        this.semaphore.acquire()

        map = this.dict.filter(predicate)

        this.semaphore.release()

        return map
    }

    fun containsKey(key: K): Boolean {
        var result: Boolean

        this.semaphore.acquire()

        result = this.dict.containsKey(key)

        this.semaphore.release()

        return result
    }
}