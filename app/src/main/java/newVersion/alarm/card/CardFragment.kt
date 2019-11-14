package newVersion.alarm.card

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.utils.Alarm

class CardFragment : MvpAppCompatFragment(), CardView {

    @InjectPresenter
    lateinit var presenter: CardPresenter

    lateinit var bReport: Button
    lateinit var bArrived: Button
    var rootView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_card, container, false)

        bReport = rootView?.findViewById(R.id.card_send_reports)!!
        bArrived = rootView?.findViewById(R.id.card_send_arrived)!!

        bReport.setOnClickListener {
            Log.d("Report","send")
            presenter.sendAction("Report")
        }
        bArrived.setOnClickListener {
            Log.d("Arrived","send")
            presenter.sendAction("Arrived")
        }

        return rootView
    }
    override fun onResume() {
        super.onResume()

        if (!presenter.init && activity?.intent?.hasExtra("info")!!) {
            presenter.init(activity?.intent?.getSerializableExtra("info") as Alarm)
        }
        else
        {
            presenter.init(null)
        }
    }
    override fun setStateReportButton(enable: Boolean) {
        bReport.isEnabled = enable
    }

    override fun setStateArrivedButton(enable: Boolean) {
        bArrived.isEnabled = enable
    }

    override fun setObjectName(name: String) {
        val tvObjectName: TextView = rootView?.findViewById(R.id.card_object_name)!!
        tvObjectName.text = name
    }

    override fun setObjectAddress(address: String) {
        val tvObjectAddress: TextView = rootView?.findViewById(R.id.card_object_address)!!
        tvObjectAddress.text = address
    }

    override fun setObjectNumber(number: String) {
        val tvObjectNumber: TextView = rootView?.findViewById(R.id.card_object_number)!!
        tvObjectNumber.text = number
    }

    override fun setObjectCustom(customer: String) {
        val tvObjectCustom: TextView = rootView?.findViewById(R.id.card_object_customer)!!
        tvObjectCustom.text = customer
    }

    override fun setObjectInn(inn: String) {
        val tvObjectInn: TextView = rootView?.findViewById(R.id.card_object_inn)!!
        tvObjectInn.text = inn
    }

    override fun setObjectAlarm(alarm: String) {
        val tvObjectAlarm: TextView = rootView?.findViewById(R.id.card_object_alarm)!!
        tvObjectAlarm.text = alarm
    }

    override fun setObjectTimeAlarm(timeAlarm: String) {
        val tvObjectAlarmTime: TextView = rootView?.findViewById(R.id.card_object_alarm_time)!!
        tvObjectAlarmTime.text = timeAlarm
    }

    override fun setObjectTimeApplyAlarm(timeApplyAlarm: String) {
        val tvObjectAlarmApplyTime: TextView = rootView?.findViewById(R.id.card_object_alarm_apply_time)!!
        tvObjectAlarmApplyTime.text = timeApplyAlarm
    }

    override fun setObjectTimeArrived(timeArrived: String) {
        val tvObjectArrivedTime: TextView = rootView?.findViewById(R.id.card_object_time_arrived)!!
        tvObjectArrivedTime.text = timeArrived
    }

    override fun setObjectAdditionally(additionally: String) {
        val tvObjectAdditionally: TextView = rootView?.findViewById(R.id.card_object_additionally)!!
        tvObjectAdditionally.text = additionally
    }

    override fun onPause() {
        presenter.onDestroy()
        super.onPause()
    }
}