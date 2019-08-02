package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SampleFragmentPagerAdapter(fm: FragmentManager?, private val context: Context) : androidx.fragment.app.FragmentPagerAdapter(fm!!) {

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