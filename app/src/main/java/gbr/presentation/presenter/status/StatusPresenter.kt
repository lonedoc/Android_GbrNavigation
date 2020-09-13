package gbr.presentation.presenter.status

import android.os.CountDownTimer
import android.util.Log
import gbr.presentation.view.status.StatusView
import gbr.utils.api.status.OnStatusListener
import gbr.utils.api.status.RPStatusAPI
import gbr.utils.api.status.StatusAPI
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.utils.DataStoreUtils
import gbr.utils.interfaces.DestroyableAPI
import newVersion.commonInterface.Init
import rubegprotocol.RubegProtocol

@InjectViewState
class StatusPresenter : MvpPresenter<StatusView>(), OnStatusListener,
    DestroyableAPI, Init {
    override fun onStatusDataReceived(status: String, call: String) {
        Log.d("StatusPresenter", status)
        if(status =="На тревоге") return
            DataStoreUtils.status = status
            DataStoreUtils.call = call
            viewState.onDismiss()
    }

    var statusAPI: StatusAPI? = null

    override var init: Boolean = false

    override fun isInit(): Boolean {
        return init
    }

    fun setTimer(time: Long) {
        val protocol = RubegProtocol.sharedInstance
        if (statusAPI != null) statusAPI?.onDestroy()
        statusAPI = RPStatusAPI(protocol)
        statusAPI?.onStatusListener = this

        init = true
        var timer: CountDownTimer? = null

        timer?.cancel()

        timer = object : CountDownTimer(time*60000, 1000) {
            override fun onFinish() {
                sendRequest()
                onDestroy()
                viewState.onDismiss()
            }

            override fun onTick(time: Long) {
                viewState.updateTimer((time / 1000).toInt())
            }
        }

        timer.start()
    }

    fun sendRequest() {
        statusAPI?.sendStatusRequest(
            "Свободен",
            complete = { success ->
                if (!success) {
                    viewState.showToastMessage("Запрос на смену статуса не был отправлен")
                }
            }
        )
    }

    override fun onDestroy() {
        statusAPI?.onDestroy()
        super.onDestroy()
    }
}