package newVersion.alarm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_alarm.*
import newVersion.Utils.DataStoreUtils
import newVersion.alarm.pager.AlarmTabFragment
import newVersion.callback.ReportCallback
import newVersion.common.CommonActivity
import oldVersion.workservice.Alarm

class AlarmActivity : MvpAppCompatActivity(), AlarmView,ReportCallback {
    override fun recallActivity(alarmInfo: Alarm?) {
        val recallActivity = Intent( applicationContext,AlarmActivity::class.java)
        recallActivity.putExtra("infi",alarmInfo)
        recallActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(recallActivity)
    }

    @InjectPresenter
    lateinit var presenter: AlarmPresenter

    companion object {
        var elapsedMillis: Long? = null
        var isAlive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        setSupportActionBar(alarm_toolbar)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

    override fun openFragment(fragment: MvpAppCompatFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.alarm_fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun startTimer() {
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

    override fun completeAlarm() {
        runOnUiThread {
            alarm_timer.stop()

            elapsedMillis = 0

            intent.removeExtra("info")

            presenter.onDestroy()

            val intent = Intent(applicationContext, CommonActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        openFragment(AlarmTabFragment())
        alarm_bottom_menu.menu.getItem(0).isChecked = true
    }
}