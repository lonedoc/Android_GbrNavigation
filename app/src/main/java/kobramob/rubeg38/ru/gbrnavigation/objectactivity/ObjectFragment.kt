package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kobramob.rubeg38.ru.gbrnavigation.R

class ObjectFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bnv: BottomNavigationView = activity!!.findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true
        return inflater.inflate(R.layout.object_fragment, container, false)
    }
}