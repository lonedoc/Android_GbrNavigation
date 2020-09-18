package gbr.ui.alarm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import gbr.presentation.presenter.alarm.AlarmPresenter
import gbr.presentation.view.alarm.AlarmView
import gbr.ui.navigator.NavigatorFragment
import gbr.ui.pager.AlarmTabFragment
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.activity_new_alarm.*
import moxy.MvpAppCompatActivity
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter

class AlarmActivity:MvpAppCompatActivity(),AlarmView {
    @InjectPresenter
    lateinit var presenter:AlarmPresenter

    var isAlive:Boolean = false

    private val navigatorFragment: NavigatorFragment = NavigatorFragment()
    private val tabFragment:AlarmTabFragment = AlarmTabFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_alarm)
        setSupportActionBar(new_alarm_toolbar)

       openFragment(AlarmTabFragment())

        new_alarm_bottom_menu.menu.getItem(0).isChecked = true

        new_alarm_bottom_menu.setOnNavigationItemSelectedListener {
            item ->
            when(item.itemId){
                R.id.pager->{
                    openFragment(tabFragment)
                    supportActionBar!!.title="Карточка объекта"
                }
                R.id.navigator->{
                    openFragment(navigatorFragment)
                    supportActionBar!!.title="Навигатор"
                }
            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
        isAlive=true

        presenter.context(applicationContext)
    }

    //TODO НЕ РАБОТАЕТ НА 11 АНДРОИДЕ FLAG_TASK
    override fun openFragment(fragment: MvpAppCompatFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.alarm_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun showBottomBar(view: Int) {
        new_alarm_bottom_menu.visibility = view
    }

    override fun startTimer(elapsedRealtime: Long) {
        runOnUiThread {
            new_alarm_timer.start()
            new_alarm_timer.base = elapsedRealtime
        }
    }

    override fun showToastMessage(message:String) {
        runOnUiThread {
            Toast.makeText(this,message,Toast.LENGTH_LONG).show()
        }
    }
    override fun onStop() {
        super.onStop()
        isAlive=false
    }


}