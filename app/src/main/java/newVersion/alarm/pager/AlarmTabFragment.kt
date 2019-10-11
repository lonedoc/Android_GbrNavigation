package newVersion.alarm.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.arellomobile.mvp.MvpAppCompatFragment
import com.google.android.material.tabs.TabLayout
import kobramob.rubeg38.ru.gbrnavigation.R

class AlarmTabFragment : MvpAppCompatFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.alarm_tab_fragment, container, false)

        val viewPager: ViewPager = rootView.findViewById(R.id.alarm_pager)
        viewPager.adapter = AlarmPagerAdapter(childFragmentManager)

        val tabLayout: TabLayout = rootView.findViewById(R.id.alarm_tab)
        tabLayout.setupWithViewPager(viewPager)

        return rootView
    }
}