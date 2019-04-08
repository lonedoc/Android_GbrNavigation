package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kobramob.rubeg38.ru.gbrnavigation.R

class TabFragment : Fragment() {
    private val objectFragment: ObjectFragment = ObjectFragment()
    private val feedFragment: FeedFragment = FeedFragment()
    private val responsibleFragment: ResponsibleFragment = ResponsibleFragment()
    private val planFragment: PlanFragment = PlanFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.tab_fragment, container, false)

        val viewPager: ViewPager = rootView.findViewById(R.id.pager)
        viewPager.adapter = SampleFragmentPagerAdapter(childFragmentManager, context!!)

        val tablLayout: TabLayout = rootView.findViewById(R.id.testTab)
        tablLayout.setupWithViewPager(viewPager)
      /*  tablLayout.getTabAt(0)!!.setIcon(R.drawable.ic_object)
        tablLayout.getTabAt(1)!!.setIcon(R.drawable.ic_responsible)
        tablLayout.getTabAt(2)!!.setIcon(R.drawable.ic_plan)*/
        return rootView
    }
}