package gbr.ui.mobobjectinfo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import gbr.presentation.presenter.mobobjectinfo.MobObjectInfoPresenter
import gbr.presentation.view.mobobjectinfo.MobObjectInfoView
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.models.CardEvent
import org.greenrobot.eventbus.EventBus


class FragmentMobObjectInfo: MvpAppCompatFragment(),MobObjectInfoView {
    @InjectPresenter
    lateinit var presenter:MobObjectInfoPresenter
    lateinit var rootView:View
    lateinit var imageView:ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_mobobjectinfo,container,false)

        imageView = rootView.findViewById(R.id.mob_card_object_image)
        return rootView
    }

    override fun setImage(bitmap: Bitmap) {
        activity!!.runOnUiThread {
            imageView.setImageBitmap(bitmap)
        }
    }

    override fun setPhone(phone: String?) {
        val phoneTW = rootView.findViewById<TextView>(R.id.mob_card_object_phone)
        phoneTW.text = phone
    }

    override fun setName(name: String?) {
        val nameTW = rootView.findViewById<TextView>(R.id.mob_card_object_name)
        nameTW.text = name
    }

    override fun setObjectTimeApplyAlarm(currentTime: String) {
        val currentTimeTW = rootView.findViewById<TextView>(R.id.mob_card_object_alarm_apply)
        currentTimeTW.text = currentTime
    }

    override fun setObjectTimeArrived(arrivedTime: String) {
        val arrivedTimeTW = rootView.findViewById<TextView>(R.id.mob_card_object_alarm_arrived)
        arrivedTimeTW.text = arrivedTime
    }
}