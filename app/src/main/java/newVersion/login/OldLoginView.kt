package newVersion.login

import moxy.MvpView
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import newVersion.models.HostPool

interface OldLoginView : MvpView {

    fun setAddress(address: ArrayList<String>?)

    fun setPort(port: String?)

    fun setPortTextViewError(message: String?)

    fun showToastMessage(message: String?)

    @StateStrategyType(value = SkipStrategy::class)
    fun startService(credentials: newVersion.models.Credentials, hostPool: HostPool)

    fun disconnectServer()

    @StateStrategyType(value = SingleStateStrategy::class)
    fun openCommonScreen()

    fun showDialog()

    fun closeDialog()

    fun visibilityAddButton(View: Int)

    fun visibilityRemoveButton(View: Int)

    @StateStrategyType(value = SkipStrategy::class)
    fun removeItem(indexItem: Int, size: Int)

    @StateStrategyType(value = SkipStrategy::class)
    fun addItem(address: java.util.ArrayList<String>)
}