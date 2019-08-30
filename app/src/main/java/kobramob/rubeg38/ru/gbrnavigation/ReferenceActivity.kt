package kobramob.rubeg38.ru.gbrnavigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.AlarmObjectInfo
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.workservice.DataStore

class ReferenceActivity : AppCompatActivity() {

    private val referenceTabFragment: ReferenceTabFragment = ReferenceTabFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reference)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val toolbar: Toolbar = findViewById(R.id.reference_toolbar)
        setSupportActionBar(toolbar)

        val title:String = DataStore.cityCard.pcsinfo.name
        supportActionBar?.title = title

        openFragment(referenceTabFragment)

    }

    private fun openFragment(fragment:Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.reference_fragment_container,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        if(!intent.getBooleanExtra("objectActivity",false)){
            val commonActivity = Intent(this, CommonActivity::class.java)
            startActivity(commonActivity)
        }
        else
        {
            val objectInfo:AlarmObjectInfo = intent.getSerializableExtra("objectInfo") as AlarmObjectInfo
            val objectActivity = Intent(this, ObjectActivity::class.java)
            objectActivity.putExtra("objectInfo",objectInfo)
            startActivity(objectActivity)
        }

    }
}

@SuppressLint("WrongConstant")
class ReferenceFragmentPagerAdapter(fm: FragmentManager?):
    FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabTitles = arrayOf("Дежурная часть", "Экстренные службы", "Коммунальные службы")

    override fun getItem(position: Int): Fragment {
        var fragment:Fragment? = null
        when(position){
            0->{fragment = PCSInfoFragment()}
            1->{fragment = ESInfoFragment()}
            2->{fragment = USInfoFragment()}
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}

class ReferenceTabFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.reference_tab_fragment, container, false)

        val viewPager: ViewPager = rootView.findViewById(R.id.reference_pager)
        viewPager.adapter = ReferenceFragmentPagerAdapter(childFragmentManager)

        val tablLayout: TabLayout = rootView.findViewById(R.id.referenceTab)
        tablLayout.setupWithViewPager(viewPager)

        return rootView
    }
}
