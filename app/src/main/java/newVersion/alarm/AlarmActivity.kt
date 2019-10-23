package newVersion.alarm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_alarm.*
import newVersion.utils.Alarm
import newVersion.utils.DataStoreUtils
import newVersion.alarm.card.ArrivedTime
import newVersion.alarm.pager.AlarmTabFragment
import newVersion.alarm.plan.ImageScaleFragment
import newVersion.alarm.plan.PlanPresenter
import newVersion.alarm.plan.PlanPresenter.Companion.plan
import newVersion.callback.ReportCallback
import newVersion.common.CommonActivity
import org.greenrobot.eventbus.EventBus


class AlarmActivity : MvpAppCompatActivity(), AlarmView,ReportCallback {
    override fun recallActivity(alarmInfo: Alarm?) {

    }

    @InjectPresenter
    lateinit var presenter: AlarmPresenter

    companion object {
        var elapsedMillis: Long? = null
        var isAlive = false
    }

    private val alarmTabFragment: AlarmTabFragment =
        AlarmTabFragment()
    private val navigatorFragment: newVersion.alarm.navigator.NavigatorFragment =
        newVersion.alarm.navigator.NavigatorFragment ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        setSupportActionBar(alarm_toolbar)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        alarm_bottom_menu.menu.getItem(0).isChecked = true

        alarm_bottom_menu.setOnNavigationItemSelectedListener {
            item ->
            when(item.itemId){
                R.id.cardObject->{
                    openFragment(alarmTabFragment)
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

    override fun onResume() {
        super.onResume()
        isAlive = true
        if (!presenter.init) {
            presenter.init(intent?.getSerializableExtra("info") as Alarm,applicationContext)
        }
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
    }
    override fun setTitle(title: String) {
        alarm_toolbar.title = title
    }



    override fun startTimer() {
        Log.d("AlarmActivity", "$elapsedMillis")

        if(intent.hasExtra("elapsedMillis"))
            elapsedMillis = intent.getLongExtra("elapsedMillis",0)

        if (elapsedMillis != null) {
            alarm_timer.base = SystemClock.elapsedRealtime() - elapsedMillis!!
        } else {
            alarm_timer.base = SystemClock.elapsedRealtime()
        }

        alarm_timer.setOnChronometerTickListener {
            elapsedMillis = (SystemClock.elapsedRealtime() - it.base)
        }
        alarm_timer.start()
    }

    override fun sendReport(selectedReport: String, comment: String) {
        presenter.sendReport(selectedReport,comment)
    }

    override fun reportNotSend() {
        showToastMessage("Рапорт не будет отправлен")
    }

    override fun showReportDialog() {
        if(DataStoreUtils.reports.count()<1)
        {
            showToastMessage("Невозможно отправить рапорт, список не был заполнен")
            presenter.changeStateButton(
                enableArrived = false,
                enableReport = false
            )
            return
        }
        val dialog = AlarmReportFragment.newInstance(DataStoreUtils.reports,this)
        dialog.show(supportFragmentManager,"AlarmReportFragment")
    }

    override fun showArrivedDialog() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Автоматическое прибытие")
                .setMessage("Вы прибыли на месте, выберите действие")
                .setNeutralButton("Отложить"){
                        dialogInterface, i ->
                    showToastMessage("Отправка прибытия отложена, теперь вы можете отправить его с экрана основной информации объекта")
                    presenter.changeStateButton(enableArrived = true, enableReport = false)
                }
                .setPositiveButton("Отправить"){
                        dialogInterface, i ->
                    EventBus.getDefault().postSticky(ArrivedTime((elapsedMillis?.div(1000))?.toInt()!!))
                    presenter.sendArrived()
                }
                .show()
        }

    }

    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun completeAlarm(alarm:Alarm?) {
        runOnUiThread {
            alarm_timer.stop()

            elapsedMillis = 0

            if(plan.count()>0)
            plan.clear()

            PlanPresenter.countQueueImageInDownload = 0

            intent.removeExtra("info")

            presenter.onDestroy()

            val intent = Intent(applicationContext, CommonActivity::class.java)
            if(alarm!=null)
                intent.putExtra("alarm",alarm)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {

        when {
            ImageScaleFragment.isAlive -> {
                supportFragmentManager.popBackStack()
            }
            alarmTabFragment.haveChild() -> {
                alarmTabFragment.onBackPressed()
            }
        }
    }

    override fun openFragment(fragment: MvpAppCompatFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.alarm_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}