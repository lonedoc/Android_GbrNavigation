package kobramob.rubeg38.ru.networkprotocol

import android.util.Pair
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

enum class Priority {
    LOW, MEDIUM, HIGH
}

class PriorityQueue<T> {
    private val array: ArrayList<Pair<T, Priority>>
    private val semaphore: Semaphore

    constructor() {
        this.array = ArrayList()
        this.semaphore = Semaphore(1)
    }

    fun enqueue(item: T) {
        this.enqueue(item, Priority.LOW)
    }

    fun enqueue(item: T, priority: Priority) {
        var i = 0
        while (true) {
            this.semaphore.acquire()

            if (i >= this.array.count()) {
                this.array.add(Pair(item, priority))

                this.semaphore.release()

                return
            }

            if (this.array[i].second < priority) {
                this.array.add(i, Pair(item, priority))

                this.semaphore.release()

                return
            }

            this.semaphore.release()
            i++
        }
    }

    fun dequeue(): T? {
        this.semaphore.acquire()

        val item = if (this.array.count() > 0) this.array.removeAt(0).first else null


        if (this.array.count() >= 2) {
            println("[0]: { messageNumber: ${(this.array[0].first as Packet).headers.messageNumber}, packetNumber: ${(this.array[0].first as Packet).headers.packetNumber} }")
            println("[1]: { messageNumber: ${(this.array[1].first as Packet).headers.messageNumber}, packetNumber: ${(this.array[1].first as Packet).headers.packetNumber} }")
        }

        /*if(item!=null)
            println("dequeue {messageNumber: ${(item as Packet?)!!.headers.messageNumber}, packetNumber: ${(item as Packet?)!!.headers.packetNumber}} ")*/

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