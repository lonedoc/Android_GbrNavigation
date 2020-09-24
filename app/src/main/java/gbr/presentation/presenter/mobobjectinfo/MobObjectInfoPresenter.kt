package gbr.presentation.presenter.mobobjectinfo

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import gbr.presentation.view.mobobjectinfo.MobObjectInfoView
import gbr.ui.main.MainActivity
import gbr.utils.api.alarm.AlarmAPI
import gbr.utils.api.alarm.OnAlarmListener
import gbr.utils.api.alarm.RPAlarmAPI
import gbr.utils.api.status.OnStatusListener
import gbr.utils.api.status.RPStatusAPI
import gbr.utils.api.status.StatusAPI
import gbr.utils.data.AlarmInfo
import gbr.utils.data.Info
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.common.CurrentTime
import org.greenrobot.eventbus.EventBus
import rubegprotocol.RubegProtocol
import java.text.SimpleDateFormat
import java.util.*

@InjectViewState
class MobObjectInfoPresenter:MvpPresenter<MobObjectInfoView>(){

}