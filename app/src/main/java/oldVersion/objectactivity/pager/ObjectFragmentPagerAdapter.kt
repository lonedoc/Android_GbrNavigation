package objectactivity.pager

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import objectactivity.fragment.ObjectFragment
import objectactivity.planfragment.PlanFragment
import objectactivity.responsiblefragment.ResponsibleFragment

class ObjectFragmentPagerAdapter(fm: FragmentManager?, private val context: Context) : FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 3
    }

    private val tabTitles = arrayOf("Карточка объекта", "Ответственные", "План - схема")

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> { fragment = ObjectFragment()
            }
            1 -> { fragment = ResponsibleFragment()
            }
            2 -> { fragment = PlanFragment()
            }
        }
        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}