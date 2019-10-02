package newVersion.main

import com.arellomobile.mvp.MvpView
import newVersion.models.Credentials
import newVersion.models.HostPool

interface MainView : MvpView {
    fun showToastMessage(message: String)
    fun setHintText(message: String)
    fun checkPermission()
    fun startService(credentials: Credentials, hostPool: HostPool)
    fun startAnimation()
    fun initLocationManager()
    fun openLoginActivity()
    fun openCommonActivity()
    fun disconnectServer()
}