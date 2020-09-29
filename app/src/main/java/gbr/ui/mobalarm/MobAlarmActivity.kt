package gbr.ui.mobalarm

import android.os.Bundle
import android.widget.Toast
import gbr.presentation.presenter.mobalarm.MobAlarmPresenter
import gbr.presentation.view.mobalarm.MobAlarmView
import gbr.ui.mobnavigator.FragmentMobNavigator
import gbr.ui.mobobjectinfo.FragmentMobObjectInfo
import gbr.ui.objectinfo.FragmentObjectInfo
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_mob_alarm.*
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.activity_alarm.new_alarm_bottom_menu
import moxy.MvpAppCompatActivity
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter

class MobAlarmActivity:MvpAppCompatActivity(), MobAlarmView {
    @InjectPresenter
    lateinit var presenter:MobAlarmPresenter

    private val objectInfo = FragmentMobObjectInfo()
    private val navigator = FragmentMobNavigator()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mob_alarm)
        setSupportActionBar(mob_alarm_toolbar)
        openFragment(objectInfo)

        mob_alarm_bottom_menu.menu.getItem(0).isChecked = true

        mob_alarm_bottom_menu.setOnNavigationItemSelectedListener {
                item ->
            when(item.itemId){
                R.id.pager->{
                    openFragment(objectInfo)
                    setTitle("Карточка объекта")
                }
                R.id.navigator->{
                    openFragment(navigator)
                    setTitle("Навигатор")
                }
            }
            true
        }
    }

    fun openFragment(fragment: MvpAppCompatFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mob_alarm_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onStart() {
        super.onStart()
        //isAlive=true

        presenter.context(applicationContext)
    }

    override fun showBottomBar(view: Int) {
        mob_alarm_bottom_menu.visibility = view
    }

    override fun setTitle(title: String) {
        supportActionBar!!.title=title
    }

    override fun stateArrived(state: Boolean) {
        sendArrived.isEnabled = state
    }

    override fun stateReport(state: Boolean) {
        sendReport.isEnabled = state
    }

    override fun statePhoto(state: Boolean) {
        sendPhoto.isEnabled = state
    }

    override fun startTimer(elapsedRealtime: Long) {
        runOnUiThread {
            mob_alarm_timer.start()
            mob_alarm_timer.base = elapsedRealtime
        }
    }

    override fun showToastMessage(message:String) {
        runOnUiThread {
            Toast.makeText(this,message, Toast.LENGTH_LONG).show()
        }
    }

}