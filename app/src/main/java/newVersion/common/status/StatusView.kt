package newVersion.common.status

import com.arellomobile.mvp.MvpView

interface StatusView : MvpView {
    fun showToastMessage(message: String)
    fun onDismiss()
    fun updateTimer(time: Int)
}