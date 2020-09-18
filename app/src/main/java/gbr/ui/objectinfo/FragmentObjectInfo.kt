package gbr.ui.objectinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import gbr.presentation.presenter.objectinfo.ObjectInfoPresenter
import gbr.presentation.view.objectinfo.ObjectInfoView
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.models.CardEvent
import org.greenrobot.eventbus.EventBus

class FragmentObjectInfo:MvpAppCompatFragment(), ObjectInfoView {
    @InjectPresenter
    lateinit var presenter:ObjectInfoPresenter

    lateinit var rootView:View

    lateinit var bReport: Button
    lateinit var bArrived: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_objectinfo,container,false)

        bReport = rootView.findViewById(R.id.new_card_send_reports)!!
        bArrived = rootView.findViewById(R.id.new_card_send_arrived)!!

        bArrived.setOnClickListener {
            EventBus.getDefault().post(CardEvent("Arrived"))
            setStateArrivedButton(false)
            setStateReportButton(true)
        }
        bReport.setOnClickListener {
            EventBus.getDefault().post(CardEvent("Report"))
            setStateReportButton(false)
        }
        return rootView
    }

    override fun setObjectName(name: String) {
        val tvObjectName: TextView = rootView.findViewById(R.id.new_card_object_name)!!
        tvObjectName.text = name
    }

    override fun setObjectAddress(address: String) {
        val tvObjectAddress: TextView = rootView.findViewById(R.id.new_card_object_address)!!
        tvObjectAddress.text = address
    }

    override fun setObjectNumber(number: String) {
        val tvObjectNumber: TextView = rootView.findViewById(R.id.new_card_object_number)!!
        tvObjectNumber.text = number
    }

    override fun setObjectCustom(zakaz: String) {
        val tvObjectCustom: TextView = rootView.findViewById(R.id.new_card_object_customer)!!
        tvObjectCustom.text = zakaz
    }

    override fun setObjectInn(inn: String) {
        val tvObjectInn: TextView = rootView.findViewById(R.id.new_card_object_inn)!!
        tvObjectInn.text = inn
    }

    override fun setObjectAlarm(alarmName: String) {
        val tvObjectAlarm: TextView = rootView.findViewById(R.id.new_card_object_alarm)!!
        tvObjectAlarm.text = alarmName
    }

    override fun setObjectTimeAlarm(alarmTime: String) {
        val tvObjectAlarmTime: TextView = rootView.findViewById(R.id.new_card_object_alarm_time)!!
        tvObjectAlarmTime.text = alarmTime
    }

    override fun setObjectTimeApplyAlarm(currentTime: String) {
        val tvObjectAlarmApplyTime: TextView = rootView.findViewById(R.id.new_card_object_alarm_apply_time)!!
        tvObjectAlarmApplyTime.text = currentTime
    }

    override fun setObjectTimeArrived(arrivedTime: String) {
        val tvObjectArrivedTime: TextView = rootView.findViewById(R.id.new_card_object_time_arrived)!!
        tvObjectArrivedTime.text = arrivedTime
    }

    override fun setObjectAdditionally(additionally: String) {
        val tvObjectAdditionally: TextView = rootView.findViewById(R.id.new_card_object_additionally)!!
        tvObjectAdditionally.text = additionally
    }

    override fun setStateReportButton(state: Boolean) {
        bReport.isEnabled = state
    }

    override fun setStateArrivedButton(state: Boolean) {
        bArrived.isEnabled = state
    }
}