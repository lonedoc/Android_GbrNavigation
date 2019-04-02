package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class SampleFragmentPagerAdapter(fm: FragmentManager?, private val context: Context) : FragmentPagerAdapter(fm) {


    override fun getCount(): Int {
        return 4
    }

    private val tabTitles = arrayOf("Карточка объекта", "Лента событий", "Ответственные","План - схема")

    override fun getItem(position: Int): Fragment {
        var fragment:Fragment? = null
        when(position){
            0->{fragment = ObjectFragment()}
            1->{fragment = FeedFragment()}
            2->{fragment = ResponsibleFragment()}
            3->{fragment = PlanFragment()}
        }
        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position]
    }
}