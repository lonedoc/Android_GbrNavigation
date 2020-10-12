package gbr.presentation.view.login

import moxy.MvpView
import java.util.ArrayList

interface LoginView:MvpView {

    fun setAddress(address: ArrayList<String>)

    fun setImei()

    fun setImeiTextViewError(message: String?)

    fun setPort(port:String)

    fun setPortTextViewError(message: String?)

    fun visibilityAddButton(visibility: Int)

    fun visibilityRemoveButton(visibility: Int)

    fun showToastMessage(message: String)

    fun mainActivity()

    fun showDialog()

    fun closeDialog()

}
