package gbr.ui.navigator

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gbr.presentation.view.navigator.NavigatorView
import gbr.utils.data.AlarmInfo
import gbr.utils.servicess.YandexNavigator
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.new_fragment_navigator.*
import moxy.MvpAppCompatFragment

class NavigatorFragment:MvpAppCompatFragment(),NavigatorView {

    var rootView:View? = null
    val alarmInfo = AlarmInfo
    val yandexNavigator:YandexNavigator = YandexNavigator()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.new_fragment_navigator,container,false)

        yandexNavigator()
     /*   navigator_yandex.setOnClickListener {
            yandexNavigator()
        }

        navigator_navitel.setOnClickListener {
            navitelNavigator()
        }*/


        return rootView
    }

    private fun navitelNavigator() {
        val uri = Uri.parse("google.navigation:ll=${alarmInfo.lat},${alarmInfo.lon}")
        var intent = Intent(Intent.ACTION_VIEW,uri)
        intent.setPackage("com.navitel")
        if(intent.resolveActivity(activity!!.packageManager) == null)
        {
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.navitel")
        }
        activity!!.startActivity(intent)
    }

    private fun yandexNavigator() {
        yandexNavigator.buildRoute(activity!!,alarmInfo.lat!!,alarmInfo.lon!!)
    }

}