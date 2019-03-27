package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kobramob.rubeg38.ru.gbrnavigation.R

class AdapterFeedList internal constructor(private val eventList: ArrayList<String>) : RecyclerView.Adapter<AdapterFeedList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_feed, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onBindViewHolder(holder:  ViewHolder, position: Int) {

        holder.textEvent.text = eventList[position]

    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        var textEvent:TextView = itemView.findViewById(R.id.textEvent)
    }

}
