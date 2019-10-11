package newVersion.alarm.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import newVersion.alarm.card.CardFragment
import oldVersion.objectactivity.planfragment.PlanFragment
import oldVersion.objectactivity.responsiblefragment.ResponsibleFragment

class AlarmPagerAdapter(childFragmentManager: FragmentManager) : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 3
    }

    private val tabTitles = arrayOf("Карточка объекта", "Ответственные", "План - схема")

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 ->
                {
                    fragment = CardFragment()
                }
            1 ->
                {
                    fragment = newVersion.alarm.responsible.ResponsibleFragment()
                }
            2 ->
                {
                    fragment = newVersion.alarm.plan.PlanFragment()
                }
        }
        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}