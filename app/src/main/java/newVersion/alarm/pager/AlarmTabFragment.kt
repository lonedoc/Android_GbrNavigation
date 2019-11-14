package newVersion.alarm.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment


class AlarmTabFragment : MvpAppCompatFragment() {


    private var adapter:AlarmPagerAdapter? = null
    private var pager:ViewPager? = null
    private var tabLayout:TabLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_alarm_tab, container, false)

        pager = rootView.findViewById(R.id.alarm_pager)
        tabLayout = rootView.findViewById(R.id.alarm_tab)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = AlarmPagerAdapter(childFragmentManager,resources)
        pager?.adapter = adapter
        tabLayout?.setupWithViewPager(pager)
    }

    fun onBackPressed(){
        pager?.setCurrentItem(pager?.currentItem!! - 1  ,false)
    }

    fun haveChild():Boolean{
        return pager?.currentItem != 0
    }
}