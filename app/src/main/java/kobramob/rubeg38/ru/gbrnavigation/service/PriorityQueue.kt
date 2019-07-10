package kobramob.rubeg38.ru.gbrnavigation.service

import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

enum class Priority(value: Int) {
    low(1), medium(2), high(3);
}
class PriorityQueue<T> {

    private var items: CopyOnWriteArrayList<Pair<T, Int>> = CopyOnWriteArrayList()

    @Synchronized fun enqueue(item: T, priority: Int) {
        for (i in 0 until items.count()) {
            if (priority > items[i].second) {
                Log.d("Enqueue>", items.count().toString())
                items.add(i, Pair(item, priority))
                return
            }
        }
        items.add(Pair(item, priority))
    }

    fun enqueue(item: T) {
        this.enqueue(item, 1)
    }

    @Synchronized fun dequeue(): T? {
        return if (items.count()> 0) {
            items.getOrNull(0)!!.first
        } else {
            null
        }
    }

    @Synchronized fun remove() {
        if(items.count()>0)
        items.removeAt(0)
    }
    @Synchronized fun removeAll(predicate: (T) -> Boolean) {
        items = CopyOnWriteArrayList(items.filter { !predicate(it.first) })
    }
}