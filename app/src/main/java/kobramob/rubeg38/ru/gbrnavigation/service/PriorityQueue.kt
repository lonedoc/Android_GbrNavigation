package kobramob.rubeg38.ru.gbrnavigation.service

import android.content.ClipData
import android.renderscript.RenderScript
import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

enum class Priority(value: Int) {
    low(1),medium(2),high(3);
}
class PriorityQueue<T> {



    private lateinit var lockQueue:Job
    private var items:ArrayList<Pair<T,Int>> = ArrayList()

    fun enqueue (item:T,priority: Int){
        runBlocking {
            val async = async {
                for(i in 0 until items.count())
                {
                    if(priority > items[i].second){
                        items.add(i,Pair(item,priority))
                        return@async
                    }
                }
                items.add(Pair(item,priority))
                println(items.count())
            };awaitAll(async)
            /*lockQueue = launch {


            }
            lockQueue.join()*/
        }
    }
    fun enqueue(item:T){
        this.enqueue(item,1)
    }
    fun dequeue():T? = runBlocking{
        var item:T? = null

            val lockQueue = async {
                item = if(items.count() > 0 ){
                    println("return data")
                    items.removeAt(0).first
                }
                else
                {
                    null
                }
            };awaitAll(lockQueue)
        return@runBlocking item
    }

    fun remove(){
        items.removeAt(0)
    }
    fun removeAll(predicate:(T)->Boolean) {
        runBlocking {
            val lockQueue = async {
                items = ArrayList(items.filter { !predicate(it.first) })
            };awaitAll(lockQueue)
        }

    }
}