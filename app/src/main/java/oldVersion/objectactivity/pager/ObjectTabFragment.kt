package oldVersion.objectactivity.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kobramob.rubeg38.ru.gbrnavigation.R

class ObjectTabFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.object_tab_fragment, container, false)

        val viewPager: ViewPager = rootView.findViewById(R.id.pager)
        viewPager.adapter =
            ObjectFragmentPagerAdapter(childFragmentManager)

        val tablLayout: TabLayout = rootView.findViewById(R.id.objectTab)
        tablLayout.setupWithViewPager(viewPager)

        return rootView
    }
}