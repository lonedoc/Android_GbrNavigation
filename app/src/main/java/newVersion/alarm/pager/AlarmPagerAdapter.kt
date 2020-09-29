package newVersion.alarm.pager

import android.content.res.Resources
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import gbr.ui.objectinfo.FragmentObjectInfo
import newVersion.alarm.card.CardFragment


class AlarmPagerAdapter(
    val childFragmentManager: FragmentManager,
    val resources: Resources
) : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


    override fun getCount(): Int {
        return 3
    }

    private val tabTitles = arrayOf("Карточка объекта", "Ответственные", "План - схема")
    var registeredFragments:ArrayList<Int> = ArrayList()
    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 ->
                {
                    fragment = CardFragment()
                    registeredFragments.add(0)

                }
            1 ->
                {
                    fragment = newVersion.alarm.responsible.ResponsibleFragment()
                    registeredFragments.add(1)

                }
            2 ->
                {
                    fragment = newVersion.alarm.plan.PlanFragment()
                    registeredFragments.add(2)

                }
        }
        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Fragment {
        return super.instantiateItem(container, position) as Fragment
    }

    fun haveStack():Boolean{
        Log.d("getPosition","$registeredFragments")
        return registeredFragments.count() > 1
    }

    fun getPosition():Int{
        val position = registeredFragments[0]
        Log.d("getPosition","$position")
        registeredFragments.removeAt(0)
        Log.d("getPosition","$registeredFragments")
        return position
    }
}