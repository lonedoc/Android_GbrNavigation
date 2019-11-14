package newVersion.common.status

import moxy.MvpView

interface StatusView : MvpView {
    fun showToastMessage(message: String)
    fun onDismiss()
    fun updateTimer(time: Int)
}