package gbr.ui.alarm

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.*
import gbr.presentation.presenter.alarm.AlarmPresenter
import gbr.presentation.view.alarm.AlarmView
import gbr.ui.navigator.NavigatorFragment
import gbr.ui.objectinfo.FragmentObjectInfo
import gbr.ui.pager.AlarmTabFragment
import gbr.utils.data.AlarmInfo
import gbr.utils.data.Info
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.activity_new_alarm.*
import kotlinx.android.synthetic.main.fragment_alarm_report_dialog.*
import moxy.MvpAppCompatActivity
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import kotlin.concurrent.thread

class AlarmActivity:MvpAppCompatActivity(),AlarmView {
    @InjectPresenter
    lateinit var presenter:AlarmPresenter

    var isAlive:Boolean = false

    private val navigatorFragment: NavigatorFragment = NavigatorFragment()
    private val tabFragment:AlarmTabFragment = AlarmTabFragment()

    val alarmInfo:AlarmInfo = AlarmInfo
    val info:Info = Info

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_alarm)
        setSupportActionBar(new_alarm_toolbar)

        send_Arrived.setOnClickListener { presenter.sendArrived() }

        send_Report.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.fragment_alarm_report_dialog, null)

            val report_spinner = view.findViewById<Spinner>(R.id.reports_spinner)
            val report_edittext = view.findViewById<EditText>(R.id.report_edittext)

            report_spinner.prompt = "Отправка рапорта"
            report_spinner.adapter = ArrayAdapter(
                this,
                R.layout.report_spinner_item,
                info.reportsList!!
            )

            var selectedRequest = "Пусто"

            report_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    selectedRequest =  info.reportsList!!.let { it[p2] }.toString()
                }
            }

             AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Отправить"){ dialogInterface: DialogInterface, i: Int ->
                    presenter.sendReports(selectedRequest,report_edittext.text.toString())
                }
                .setNegativeButton("Отмена"){dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.cancel()
                }
                .create()
                .show()
        }

        openFragment(AlarmTabFragment())

        new_alarm_bottom_menu.menu.getItem(0).isChecked = true

        new_alarm_bottom_menu.setOnNavigationItemSelectedListener {
            item ->
            when(item.itemId){
                R.id.pager->{
                    openFragment(tabFragment)
                    setTitle("Карточка объекта")
                }
                R.id.navigator->{
                    openFragment(navigatorFragment)
                    setTitle("Навигатор")
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

    override fun setTitle(title: String) {
        supportActionBar!!.title=title
    }

    override fun stateArrived(state: Boolean) {
        runOnUiThread {
            send_Arrived.isEnabled = state
        }
    }

    override fun stateReport(state: Boolean) {
        runOnUiThread {
            send_Report.isEnabled = state
        }
    }

    override fun statePhoto(state: Boolean) {
        runOnUiThread {
            send_Photo.isEnabled = state
        }
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

    override fun onBackPressed() {
    }

}