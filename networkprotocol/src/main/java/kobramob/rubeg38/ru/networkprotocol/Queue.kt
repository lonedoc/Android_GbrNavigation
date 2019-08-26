package kobramob.rubeg38.ru.networkprotocol

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

       /* if(item!=null)
        println("dequeue {messageNumber: ${(item as Packet?)!!.headers.messageNumber}, packetNumber: ${(item as Packet?)!!.headers.packetNumber} }")*/

        this.semaphore.release()

        return item
    }
}