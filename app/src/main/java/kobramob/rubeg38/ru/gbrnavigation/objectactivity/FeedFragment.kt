package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R

class FeedFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.feed_fragment, container, false)

        val feedList: androidx.recyclerview.widget.RecyclerView = rootView.findViewById(R.id.feedRecyclerView)

        val eventList = ArrayList<String>()
        eventList.add("Событие 1")
        eventList.add("Событие 2")
        eventList.add("Событие 3")

        feedList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        feedList.adapter = AdapterFeedList(eventList)

        feedList.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                feedList.context,
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )

        return rootView
    }
}