package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kobramob.rubeg38.ru.gbrnavigation.R

class TabFragment:Fragment() {
    private val objectFragment:ObjectFragment = ObjectFragment()
    private val feedFragment:FeedFragment = FeedFragment()
    private val responsibleFragment:ResponsibleFragment = ResponsibleFragment()
    private val planFragment:PlanFragment = PlanFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView:View = inflater.inflate(R.layout.tab_fragment, container, false)
        return rootView
    }
}