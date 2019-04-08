package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class SampleFragmentPagerAdapter(fm: FragmentManager?, private val context: Context) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 3
    }

    private val tabTitles = arrayOf("Карточка объекта", "Ответственные", "План - схема")

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> { fragment = ObjectFragment() }
            1 -> { fragment = ResponsibleFragment() }
            2 -> { fragment = PlanFragment() }
        }
        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}