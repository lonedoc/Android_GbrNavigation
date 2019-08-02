package kobramob.rubeg38.ru.gbrnavigation.service

import android.util.Pair
import java.util.*
import java.util.concurrent.Semaphore
import kotlinx.coroutines.*

enum class Priority {
    LOW, MEDIUM, HIGH
}

class PriorityQueue<T> {
    private val array: ArrayList<Pair<T, Priority>> = ArrayList()
    private val semaphore: Semaphore = Semaphore(1)

    fun enqueue(item: T) {
        this.enqueue(item, Priority.LOW)
    }

    fun enqueue(item: T, priority: Priority) {
        var i = 0
        while (true) {
            this.semaphore.acquire()

            if (i >= this.array.size - 1) {
                this.semaphore.release()

                break
            }

            if (this.array[i].second < priority)
                this.array.add(i, Pair(item, priority))

            this.semaphore.release()

            i++
        }

        this.semaphore.acquire()

        this.array.add(Pair(item, priority))

        this.semaphore.release()
    }

    fun dequeue(): T? {
        this.semaphore.acquire()

        val item = if (this.array.count() > 0) this.array.removeAt(0).first else null

        this.semaphore.release()

        return item
    }

    fun removeAll(predicate: (T) -> Boolean) {
        this.semaphore.acquire()

        // debug
        println("Clear queue. Count: ${this.array.count()}")

        this.array.removeAll { predicate(it.first) }

        // debug
        println("Queue was cleared. Count: ${this.array.count()}")

        this.semaphore.release()
    }

    fun count(): Int {
        this.semaphore.acquire()

        val count = this.array.count()

        this.semaphore.release()

        return count
    }
}