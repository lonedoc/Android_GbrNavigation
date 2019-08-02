package kobramob.rubeg38.ru.gbrnavigation.service

import java.util.concurrent.Semaphore

class Queue<T> {
    private val array: ArrayList<T> = ArrayList()
    private val semaphore: Semaphore = Semaphore(1)

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
}